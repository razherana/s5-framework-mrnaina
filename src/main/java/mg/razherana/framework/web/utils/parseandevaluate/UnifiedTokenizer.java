package mg.razherana.framework.web.utils.parseandevaluate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tokenizer that can tokenize input strings based on a unified set of keywords,
 */
public class UnifiedTokenizer {
  private final Set<String> keywords = new HashSet<>();

  private final Set<String> default_keywords = new HashSet<>(Arrays.asList(
      "+", "-", "*", "/", "%", "(", ")", "<=", ">=", "<", ">", "==", "!=", "&&", "||", "!"));
  // We need to know which keywords are single letters that need boundary checking
  private final Set<String> single_letter_keywords = new HashSet<>();

  private final Set<String> symbol_keywords = new HashSet<>();

  public UnifiedTokenizer() {
    this(List.of());
  }

  public UnifiedTokenizer(Collection<String> additionalKeywords) {
    keywords.addAll(additionalKeywords);
    initKeywords();
  }

  public List<String> tokenize(String input) {
    List<String> tokens = new ArrayList<>();
    int i = 0;
    int n = input.length();

    while (i < n) {
      char ch = input.charAt(i);

      // Skip whitespace
      if (Character.isWhitespace(ch)) {
        i++;
        continue;
      }

      // Handle quoted strings
      if (ch == '\'') {
        int start = i;
        i++; // Skip opening quote

        // Find closing quote
        while (i < n && input.charAt(i) != '\'') {
          i++;
        }

        if (i < n) {
          i++; // Skip closing quote
          tokens.add(input.substring(start, i));
        } else {
          // No closing quote
          tokens.add(input.substring(start));
        }
        continue;
      }

      // Check for symbol keywords (+, (, ), etc.)
      // These are always standalone, no boundary checking needed
      if (symbol_keywords.contains(String.valueOf(ch))) {
        tokens.add(String.valueOf(ch));
        i++;
        continue;
      }

      // Handle single-letter keywords that need boundary checking (c, b)
      if (single_letter_keywords.contains(String.valueOf(ch))) {
        // Check if it's really a standalone keyword
        boolean isStandalone = isStandaloneKeyword(input, i);

        if (isStandalone) {
          tokens.add(String.valueOf(ch));
          i++;
          continue;
        }
        // If not standalone, fall through to identifier parsing
      }

      // Handle identifiers/strings (including potential keywords that are part of
      // longer strings)
      int start = i;
      while (i < n && !Character.isWhitespace(input.charAt(i)) &&
          input.charAt(i) != '\'') {
        i++;
      }

      if (i > start) {
        String token = input.substring(start, i);
        tokens.add(token);
      }
    }

    return tokens;
  }

  private boolean isStandaloneKeyword(String input, int pos) {
    // Check character before (if exists)
    if (pos > 0) {
      char before = input.charAt(pos - 1);
      // If previous character could be part of same identifier
      if (Character.isLetterOrDigit(before) || before == '_') {
        return false;
      }
    }

    // Check character after (if exists)
    if (pos + 1 < input.length()) {
      char after = input.charAt(pos + 1);
      // If next character could be part of same identifier
      if (Character.isLetterOrDigit(after) || after == '_') {
        return false;
      }
    }

    return true;
  }

  private void initKeywords() {
    keywords.addAll(default_keywords);

    // Classify keywords
    for (String kw : keywords) {
      if (kw.length() == 1) {
        char c = kw.charAt(0);
        if (Character.isLetter(c)) {
          single_letter_keywords.add(kw);
        } else {
          symbol_keywords.add(kw); // +, (, )
        }
      } else {
        // Multi-character keywords (none in our example)
        symbol_keywords.add(kw);
      }
    }
  }
}