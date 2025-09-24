package com.syos.shared.exceptions.business;

/**
 * Exception thrown when a business rule is violated.
 * 
 * This exception represents violations of domain-specific business rules
 * that cannot be enforced through simple validation.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class BusinessRuleViolationException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    private final String ruleCode;
    private final String ruleName;
    
    /**
     * Constructs a new business rule violation exception.
     * 
     * @param ruleCode the code identifying the violated rule
     * @param ruleName the name of the violated rule
     * @param message the detail message
     */
    public BusinessRuleViolationException(String ruleCode, String ruleName, String message) {
        super(
            String.format("Business rule violation [%s]: %s - %s", ruleCode, ruleName, message),
            String.format("Operation not allowed: %s", message)
        );
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
    }
    
    /**
     * Constructs a new business rule violation exception with custom user message.
     * 
     * @param ruleCode the code identifying the violated rule
     * @param ruleName the name of the violated rule
     * @param technicalMessage the technical detail message
     * @param userMessage the user-friendly message
     */
    public BusinessRuleViolationException(String ruleCode, String ruleName, 
                                        String technicalMessage, String userMessage) {
        super(
            String.format("Business rule violation [%s]: %s - %s", ruleCode, ruleName, technicalMessage),
            userMessage
        );
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
    }
    
    /**
     * Gets the code of the violated business rule.
     * 
     * @return the rule code
     */
    public String getRuleCode() {
        return ruleCode;
    }
    
    /**
     * Gets the name of the violated business rule.
     * 
     * @return the rule name
     */
    public String getRuleName() {
        return ruleName;
    }
}