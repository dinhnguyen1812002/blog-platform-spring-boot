package com.Nguyen.blogplatform.shared.enums;



/**
 * Predefined categories for article reports.
 * Each category represents a specific type of content violation.
 */
public enum ReportCategory {
    /**
     * Spam, clickbait, or misleading content
     */
    SPAM_MISLEADING("Spam or misleading content"),
    
    /**
     * Hate speech, harassment, or inappropriate language
     */
    INAPPROPRIATE_LANGUAGE("Inappropriate language or hate speech"),
    
    /**
     * Unauthorized use of copyrighted material
     */
    COPYRIGHT_INFRINGEMENT("Copyright infringement"),
    
    /**
     * False information or misinformation
     */
    MISINFORMATION("False information/misinformation"),
    
    /**
     * Violent, dangerous, or harmful content
     */
    VIOLENCE_HARMFUL("Violence or harmful content"),
    
    /**
     * Other issues not covered by predefined categories
     * Requires detailed explanation from the reporter
     */
    OTHER("Other (requires explanation)");
    
    private final String description;
    
    ReportCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
