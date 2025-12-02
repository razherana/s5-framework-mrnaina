package mg.razherana.framework.web.utils.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mg.razherana.framework.web.utils.parseandevaluate.UnifiedTokenizer;

public class ValidationParser {
  private static final String[] operators = { "<=", ">=", "<", ">", "==", "!=", "&&", "||", "!", "(", ")", "+", "-",
      "*", "/", "%" };

  private String rule;

  private Map<String, Object> context;

  private Set<String> keywords = Set.of();

  public ValidationParser(String rule, Map<String, Object> context) {
    this.rule = rule;
    this.context = context;

    // Initialize keywords based on context keys
    this.keywords = new HashSet<>(Arrays.asList(operators));
    this.keywords.addAll(context.keySet());
  }

  public List<String> parse() {
    // Parse by keywords
    UnifiedTokenizer tokenizer = new UnifiedTokenizer(keywords);

    List<String> tokens = tokenizer.tokenize(rule);

    return tokens;
  }

  /**
   * @return the rule
   */
  public String getRule() {
    return rule;
  }

  /**
   * @param rule the rule to set
   */
  public void setRule(String rule) {
    this.rule = rule;
  }

  /**
   * @return the context
   */
  public Map<String, Object> getContext() {
    return context;
  }

  /**
   * @param context the context to set
   */
  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  /**
   * @return the keywords
   */
  public Set<String> getKeywords() {
    return keywords;
  }

  /**
   * @param keywords the keywords to set
   */
  public void setKeywords(Set<String> keywords) {
    this.keywords = keywords;
  }

}
