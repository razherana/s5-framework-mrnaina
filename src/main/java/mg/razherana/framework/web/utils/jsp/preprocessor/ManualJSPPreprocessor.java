package mg.razherana.framework.web.utils.jsp.preprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;

public class ManualJSPPreprocessor {

  private static final String BRIDGE_CALL_INVOCATION = "((" + JspFunctionBridge.class.getName()
      + ") request.getAttribute(\"jspFunctionBridge\"))";
  // Splits JSP content into code/comment/EL segments so each can be parsed in
  // isolation.
  private static final Pattern SPECIAL_SEGMENT_PATTERN = Pattern.compile(
      "<%--.*?--%>|<%@.*?%>|<%!.*?%>|<%=.*?%>|<%.*?%>|\\$\\{.*?\\}",
      Pattern.DOTALL);

  public static int processDirectory(String directoryPath) throws IOException {
    return processDirectory(directoryPath, JspFunctionBridge.getRegisteredViewNames());
  }

  public static int processDirectory(String directoryPath, Set<String> functionNames) throws IOException {
    List<String> sanitizedNames = sanitizeFunctionNames(functionNames);
    if (sanitizedNames.isEmpty()) {
      return 0;
    }

    System.out.println("[Fruits] : Processing directory " + directoryPath + " with functions: " + sanitizedNames);

    Path dir = Paths.get(directoryPath);
    if (!Files.exists(dir)) {
      throw new IOException("Directory does not exist: " + directoryPath);
    }

    int count = 0;
    final List<String> effectiveNames = sanitizedNames;
    try (Stream<Path> stream = Files.walk(dir)) {
      count = stream
          .filter(path -> path.toString().endsWith(".jsp"))
          .mapToInt(path -> {
            try {
              boolean result = processFile(path, effectiveNames);
              if (result) {
                System.out.println("[Fruits] : Processed " + path);
              } else {
                System.out.println("[Fruits] : No changes for " + path);
              }
              return result ? 1 : 0;
            } catch (IOException e) {
              System.err.println("Error processing " + path + ": " + e.getMessage());
              return 0;
            }
          })
          .sum();
    }

    return count;
  }

  public static boolean processFile(String filePath) throws IOException {
    List<String> functionNames = sanitizeFunctionNames(JspFunctionBridge.getRegisteredViewNames());
    if (functionNames.isEmpty()) {
      return false;
    }
    return processFile(Paths.get(filePath), functionNames);
  }

  public static boolean processFile(String filePath, Set<String> functionNames) throws IOException {
    List<String> sanitizedNames = sanitizeFunctionNames(functionNames);
    if (sanitizedNames.isEmpty()) {
      return false;
    }
    return processFile(Paths.get(filePath), sanitizedNames);
  }

  private static boolean processFile(Path jspPath, List<String> functionNames) throws IOException {
    String content = Files.readString(jspPath);
    String originalContent = content;

    content = processFunctionCalls(content, functionNames);

    if (!content.equals(originalContent)) {
      // Create backup
      Path backupPath = Paths.get(jspPath.toString() + ".backup");
      if (!Files.exists(backupPath)) {
        Files.copy(jspPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
      }

      // Write processed content
      Files.writeString(jspPath, content);
      System.out.println("Processed: " + jspPath);
      return true;
    }

    return false;
  }

  private static String processFunctionCalls(String content, List<String> functionNames) {
    if (content == null || content.isEmpty() || functionNames.isEmpty()) {
      return content;
    }

    Matcher matcher = SPECIAL_SEGMENT_PATTERN.matcher(content);
    StringBuilder processed = new StringBuilder(content.length());
    int lastIndex = 0;

    while (matcher.find()) {
      String leadingSegment = content.substring(lastIndex, matcher.start());
      processed.append(applyFunctionReplacements(leadingSegment, functionNames));

      String matchedSegment = matcher.group();
      processed.append(processSpecialSegment(matchedSegment, functionNames));

      lastIndex = matcher.end();
    }

    String trailingSegment = content.substring(lastIndex);
    processed.append(applyFunctionReplacements(trailingSegment, functionNames));

    return processed.toString();
  }

  private static String processSpecialSegment(String segment, List<String> functionNames) {
    if (segment == null || segment.isEmpty()) {
      return segment;
    }

    if (segment.startsWith("<%--") || segment.startsWith("<%@")) {
      return segment;
    }

    if (segment.startsWith("${") && segment.endsWith("}")) {
      return rebuildSegment(segment, "${", "}", functionNames);
    }

    if (segment.startsWith("<%=")) {
      return rebuildSegment(segment, "<%=", "%>", functionNames);
    }

    if (segment.startsWith("<%!")) {
      return rebuildSegment(segment, "<%!", "%>", functionNames);
    }

    if (segment.startsWith("<%")) {
      return rebuildSegment(segment, "<%", "%>", functionNames);
    }

    return applyFunctionReplacements(segment, functionNames);
  }

  private static String rebuildSegment(String segment, String prefix, String suffix, List<String> functionNames) {
    if (!segment.endsWith(suffix) || segment.length() <= prefix.length() + suffix.length()) {
      return segment;
    }

    String innerContent = segment.substring(prefix.length(), segment.length() - suffix.length());
    String processedInner = applyFunctionReplacements(innerContent, functionNames);

    return prefix + processedInner + suffix;
  }

  private static String applyFunctionReplacements(String content, List<String> functionNames) {
    if (content == null || content.isEmpty()) {
      return content;
    }

    String updatedContent = content;
    for (String functionName : functionNames) {
      updatedContent = replaceFunctionCalls(updatedContent, functionName);
    }

    return updatedContent;
  }

  private static String replaceFunctionCalls(String content, String functionName) {
    if (functionName == null || functionName.isEmpty()) {
      return content;
    }

    StringBuilder result = new StringBuilder();
    int searchIndex = 0;

    while (searchIndex < content.length()) {
      int callIndex = findFunctionCall(content, functionName, searchIndex);
      if (callIndex == -1) {
        result.append(content.substring(searchIndex));
        break;
      }

      result.append(content, searchIndex, callIndex);

      int openParenIndex = skipWhitespace(content, callIndex + functionName.length());
      if (openParenIndex >= content.length() || content.charAt(openParenIndex) != '(') {
        // Should not occur, but guard to avoid infinite loops.
        result.append(content.substring(callIndex, openParenIndex));
        searchIndex = openParenIndex;
        continue;
      }

      int closingIndex = findClosingParenthesis(content, openParenIndex);
      if (closingIndex == -1) {
        result.append(content.substring(callIndex));
        searchIndex = content.length();
        break;
      }

      String argsSegment = content.substring(openParenIndex + 1, closingIndex);
      String trimmedArgs = argsSegment.trim();

      result.append(buildBridgeInvocation(functionName, trimmedArgs.isEmpty(), argsSegment));

      searchIndex = closingIndex + 1;
    }

    return result.toString();
  }

  private static String buildBridgeInvocation(String functionName, boolean noArgs, String argsSegment) {
    String escapedName = escapeJavaString(functionName);
    if (noArgs) {
      return BRIDGE_CALL_INVOCATION + ".invoke(\"" + escapedName + "\", new Object[]{})";
    }
    return BRIDGE_CALL_INVOCATION + ".invoke(\"" + escapedName + "\", new Object[]{" + argsSegment + "})";
  }

  private static int findFunctionCall(String content, String functionName, int fromIndex) {
    int nameLength = functionName.length();
    int searchPosition = fromIndex;

    while (searchPosition < content.length()) {
      int potentialIndex = content.indexOf(functionName, searchPosition);
      if (potentialIndex == -1) {
        return -1;
      }

      if (!isValidFunctionToken(content, potentialIndex, nameLength)) {
        searchPosition = potentialIndex + nameLength;
        continue;
      }

      int afterNameIndex = skipWhitespace(content, potentialIndex + nameLength);
      if (afterNameIndex >= content.length() || content.charAt(afterNameIndex) != '(') {
        searchPosition = potentialIndex + nameLength;
        continue;
      }

      if (isInsideLiteralOrComment(content, potentialIndex)) {
        searchPosition = potentialIndex + nameLength;
        continue;
      }

      return potentialIndex;
    }

    return -1;
  }

  private static boolean isValidFunctionToken(String content, int index, int nameLength) {
    if (index > 0) {
      char previous = content.charAt(index - 1);
      if (Character.isLetterOrDigit(previous) || previous == '_' || previous == '$' || previous == '.') {
        return false;
      }
    }

    int afterIndex = index + nameLength;
    if (afterIndex < content.length()) {
      char next = content.charAt(afterIndex);
      if (Character.isLetterOrDigit(next) || next == '_' || next == '$') {
        return false;
      }
    }

    return true;
  }

  private static int skipWhitespace(String content, int index) {
    int current = index;
    while (current < content.length() && Character.isWhitespace(content.charAt(current))) {
      current++;
    }
    return current;
  }

  private static int findClosingParenthesis(String content, int openIndex) {
    int depth = 0;
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    boolean inLineComment = false;
    boolean inBlockComment = false;

    for (int i = openIndex; i < content.length(); i++) {
      char current = content.charAt(i);
      char next = i + 1 < content.length() ? content.charAt(i + 1) : '\0';

      if (inLineComment) {
        if (current == '\n' || current == '\r') {
          inLineComment = false;
        }
        continue;
      }

      if (inBlockComment) {
        if (current == '*' && next == '/') {
          inBlockComment = false;
          i++;
        }
        continue;
      }

      if (!inSingleQuote && !inDoubleQuote) {
        if (current == '/' && next == '/') {
          inLineComment = true;
          i++;
          continue;
        }

        if (current == '/' && next == '*') {
          inBlockComment = true;
          i++;
          continue;
        }
      }

      if (current == '\\') {
        i++;
        continue;
      }

      if (current == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
        continue;
      }

      if (current == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
        continue;
      }

      if (inSingleQuote || inDoubleQuote) {
        continue;
      }

      if (current == '(') {
        depth++;
        continue;
      }

      if (current == ')') {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }

    return -1;
  }

  private static boolean isInsideLiteralOrComment(String content, int index) {
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    boolean inLineComment = false;
    boolean inBlockComment = false;

    for (int i = 0; i < index; i++) {
      char current = content.charAt(i);
      char next = i + 1 < content.length() ? content.charAt(i + 1) : '\0';

      if (inLineComment) {
        if (current == '\n' || current == '\r') {
          inLineComment = false;
        }
        continue;
      }

      if (inBlockComment) {
        if (current == '*' && next == '/') {
          inBlockComment = false;
          i++;
        }
        continue;
      }

      if (!inSingleQuote && !inDoubleQuote) {
        if (current == '/' && next == '/') {
          inLineComment = true;
          i++;
          continue;
        }

        if (current == '/' && next == '*') {
          inBlockComment = true;
          i++;
          continue;
        }
      }

      if (current == '\\') {
        if (inSingleQuote || inDoubleQuote) {
          i++;
        }
        continue;
      }

      if (current == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
        continue;
      }

      if (current == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
        continue;
      }
    }

    return inSingleQuote || inDoubleQuote || inLineComment || inBlockComment;
  }

  private static List<String> sanitizeFunctionNames(Set<String> functionNames) {
    if (functionNames == null || functionNames.isEmpty()) {
      return Collections.emptyList();
    }

    return functionNames.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(name -> !name.isEmpty())
        .sorted(Comparator.comparingInt(String::length).reversed())
        .collect(Collectors.toList());
  }

  private static String escapeJavaString(String value) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      if (current == '\\' || current == '"') {
        builder.append('\\');
      }
      builder.append(current);
    }
    return builder.toString();
  }

  public static void restoreBackups(String directoryPath) throws IOException {
    Path dir = Paths.get(directoryPath);
    try (Stream<Path> stream = Files.walk(dir)) {
      stream.filter(path -> path.toString().endsWith(".jsp.backup"))
          .forEach(backupPath -> {
            try {
              Path originalPath = Paths.get(backupPath.toString().replace(".backup", ""));
              Files.copy(backupPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
              Files.delete(backupPath);
              System.out.println("Restored: " + originalPath);
            } catch (IOException e) {
              System.err.println("Failed to restore: " + backupPath);
            }
          });
    }
  }
}