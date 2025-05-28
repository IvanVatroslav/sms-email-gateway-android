package com.smsemailforwarder.app.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for filtering SMS messages based on user preferences
 * Supports blacklist/whitelist filtering, keyword filtering, and spam detection
 */
public class SmsFilter {
    
    private static final String TAG = "SmsFilter";
    
    private final PreferencesManager preferencesManager;
    
    // Common spam patterns (Croatian context)
    private static final String[] SPAM_PATTERNS = {
        "(?i).*besplat.*",           // Free/gratis
        "(?i).*nagrada.*",           // Prize/reward
        "(?i).*pobjednik.*",         // Winner
        "(?i).*kredit.*",            // Credit
        "(?i).*poziv.*sada.*",       // Call now
        "(?i).*hitno.*",             // Urgent
        "(?i).*ograničen.*",         // Limited
        "(?i).*ekskluziv.*",         // Exclusive
        "(?i).*bonus.*",             // Bonus
        "(?i).*promocij.*",          // Promotion
        "(?i).*klik.*link.*",        // Click link
        "(?i).*registruj.*",         // Register
        "(?i).*potvrdi.*",           // Confirm
        "(?i).*www\\.",              // Web links
        "(?i).*http.*",              // HTTP links
        "(?i).*bit\\.ly.*",          // Shortened URLs
        "(?i).*tinyurl.*",           // Shortened URLs
        "(?i).*\\d{4,}.*€.*",        // Large amounts with Euro
        "(?i).*\\d{4,}.*kn.*",       // Large amounts with Kuna
        "(?i).*\\d{4,}.*din.*"       // Large amounts with Dinar
    };
    
    // Compiled patterns for performance
    private static Pattern[] compiledSpamPatterns;
    
    static {
        compiledSpamPatterns = new Pattern[SPAM_PATTERNS.length];
        for (int i = 0; i < SPAM_PATTERNS.length; i++) {
            compiledSpamPatterns[i] = Pattern.compile(SPAM_PATTERNS[i]);
        }
    }
    
    public SmsFilter(Context context) {
        this.preferencesManager = new PreferencesManager(context);
    }
    
    /**
     * Main filtering method - determines if an SMS should be forwarded
     * @param senderNumber The phone number that sent the SMS
     * @param messageBody The content of the SMS message
     * @return true if the message should be forwarded, false if it should be filtered out
     */
    public boolean shouldForwardMessage(String senderNumber, String messageBody) {
        if (!preferencesManager.isFilterEnabled()) {
            Log.d(TAG, "Filtering disabled, forwarding message");
            return true;
        }
        
        // Normalize phone number for comparison
        String normalizedNumber = SmsFormatter.normalizePhoneNumber(senderNumber);
        
        Log.d(TAG, "Filtering message from: " + normalizedNumber);
        
        // Check message length constraints
        if (!checkMessageLength(messageBody)) {
            Log.d(TAG, "Message filtered: length constraints");
            return false;
        }
        
        // Check number-based filtering (blacklist/whitelist)
        if (!checkNumberFilter(normalizedNumber)) {
            Log.d(TAG, "Message filtered: number filter");
            return false;
        }
        
        // Check keyword filtering
        if (!checkKeywordFilter(messageBody)) {
            Log.d(TAG, "Message filtered: keyword filter");
            return false;
        }
        
        // Check spam filtering
        if (!checkSpamFilter(messageBody)) {
            Log.d(TAG, "Message filtered: spam filter");
            return false;
        }
        
        Log.d(TAG, "Message passed all filters, forwarding");
        return true;
    }
    
    /**
     * Check if message length is within configured constraints
     */
    private boolean checkMessageLength(String messageBody) {
        if (TextUtils.isEmpty(messageBody)) {
            return false;
        }
        
        int length = messageBody.length();
        int minLength = preferencesManager.getMinMessageLength();
        int maxLength = preferencesManager.getMaxMessageLength();
        
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Check number-based filtering (blacklist/whitelist)
     */
    private boolean checkNumberFilter(String normalizedNumber) {
        PreferencesManager.FilterMode filterMode = preferencesManager.getFilterMode();
        
        switch (filterMode) {
            case BLACKLIST:
                return !isNumberInSet(normalizedNumber, preferencesManager.getBlockedNumbers());
                
            case WHITELIST:
                return isNumberInSet(normalizedNumber, preferencesManager.getAllowedNumbers());
                
            case NONE:
            default:
                return true;
        }
    }
    
    /**
     * Check if a number matches any number in the given set
     * Supports partial matching for flexibility
     */
    private boolean isNumberInSet(String normalizedNumber, Set<String> numberSet) {
        if (numberSet == null || numberSet.isEmpty()) {
            return false;
        }
        
        for (String filterNumber : numberSet) {
            String normalizedFilterNumber = SmsFormatter.normalizePhoneNumber(filterNumber);
            
            // Exact match
            if (normalizedNumber.equals(normalizedFilterNumber)) {
                return true;
            }
            
            // Partial match (for cases where country code might be missing)
            if (normalizedNumber.endsWith(normalizedFilterNumber) || 
                normalizedFilterNumber.endsWith(normalizedNumber)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check keyword-based filtering
     */
    private boolean checkKeywordFilter(String messageBody) {
        Set<String> filterKeywords = preferencesManager.getFilterKeywords();
        
        if (filterKeywords == null || filterKeywords.isEmpty()) {
            return true; // No keywords to filter
        }
        
        String lowerCaseMessage = messageBody.toLowerCase();
        
        for (String keyword : filterKeywords) {
            if (!TextUtils.isEmpty(keyword) && 
                lowerCaseMessage.contains(keyword.toLowerCase())) {
                Log.d(TAG, "Message contains filtered keyword: " + keyword);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check spam filtering using predefined patterns
     */
    private boolean checkSpamFilter(String messageBody) {
        if (!preferencesManager.isFilterSpam()) {
            return true; // Spam filtering disabled
        }
        
        // Check against spam patterns
        for (Pattern pattern : compiledSpamPatterns) {
            if (pattern.matcher(messageBody).matches()) {
                Log.d(TAG, "Message matched spam pattern: " + pattern.pattern());
                return false;
            }
        }
        
        // Additional heuristic checks
        if (isLikelySpam(messageBody)) {
            Log.d(TAG, "Message flagged as spam by heuristics");
            return false;
        }
        
        return true;
    }
    
    /**
     * Heuristic spam detection
     */
    private boolean isLikelySpam(String messageBody) {
        if (TextUtils.isEmpty(messageBody)) {
            return false;
        }
        
        String lowerCase = messageBody.toLowerCase();
        
        // Check for excessive capitalization
        long upperCaseCount = messageBody.chars().filter(Character::isUpperCase).count();
        double upperCaseRatio = (double) upperCaseCount / messageBody.length();
        if (upperCaseRatio > 0.7 && messageBody.length() > 20) {
            return true;
        }
        
        // Check for excessive exclamation marks
        long exclamationCount = messageBody.chars().filter(c -> c == '!').count();
        if (exclamationCount > 3) {
            return true;
        }
        
        // Check for suspicious number patterns (like premium rate numbers)
        if (lowerCase.matches(".*\\b(090|091|092|093|094|095|096|097|098|099)\\d{6,7}\\b.*")) {
            return true;
        }
        
        // Check for multiple consecutive special characters
        if (lowerCase.matches(".*[!@#$%^&*()_+={}\\[\\]|\\\\:;\"'<>,.?/~`]{3,}.*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get filtering statistics for diagnostics
     */
    public String getFilteringStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SMS Filtering Configuration ===\n");
        sb.append("Filter Enabled: ").append(preferencesManager.isFilterEnabled()).append("\n");
        sb.append("Filter Mode: ").append(preferencesManager.getFilterMode()).append("\n");
        sb.append("Spam Filter: ").append(preferencesManager.isFilterSpam()).append("\n");
        sb.append("Min Message Length: ").append(preferencesManager.getMinMessageLength()).append("\n");
        sb.append("Max Message Length: ").append(preferencesManager.getMaxMessageLength()).append("\n");
        
        Set<String> blockedNumbers = preferencesManager.getBlockedNumbers();
        Set<String> allowedNumbers = preferencesManager.getAllowedNumbers();
        Set<String> keywords = preferencesManager.getFilterKeywords();
        
        sb.append("Blocked Numbers: ").append(blockedNumbers != null ? blockedNumbers.size() : 0).append("\n");
        sb.append("Allowed Numbers: ").append(allowedNumbers != null ? allowedNumbers.size() : 0).append("\n");
        sb.append("Filter Keywords: ").append(keywords != null ? keywords.size() : 0).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Test if a message would be filtered (for testing purposes)
     */
    public FilterResult testMessage(String senderNumber, String messageBody) {
        FilterResult result = new FilterResult();
        result.originalSender = senderNumber;
        result.normalizedSender = SmsFormatter.normalizePhoneNumber(senderNumber);
        result.messageBody = messageBody;
        result.shouldForward = true;
        result.filterReasons = new StringBuilder();
        
        if (!preferencesManager.isFilterEnabled()) {
            result.filterReasons.append("Filtering disabled\n");
            return result;
        }
        
        // Test each filter
        if (!checkMessageLength(messageBody)) {
            result.shouldForward = false;
            result.filterReasons.append("Message length out of range\n");
        }
        
        if (!checkNumberFilter(result.normalizedSender)) {
            result.shouldForward = false;
            result.filterReasons.append("Number filtered (blacklist/whitelist)\n");
        }
        
        if (!checkKeywordFilter(messageBody)) {
            result.shouldForward = false;
            result.filterReasons.append("Contains filtered keyword\n");
        }
        
        if (!checkSpamFilter(messageBody)) {
            result.shouldForward = false;
            result.filterReasons.append("Detected as spam\n");
        }
        
        if (result.shouldForward) {
            result.filterReasons.append("Message passed all filters\n");
        }
        
        return result;
    }
    
    /**
     * Result class for filter testing
     */
    public static class FilterResult {
        public String originalSender;
        public String normalizedSender;
        public String messageBody;
        public boolean shouldForward;
        public StringBuilder filterReasons;
        
        @Override
        public String toString() {
            return "FilterResult{\n" +
                   "  sender='" + originalSender + "' (normalized: '" + normalizedSender + "')\n" +
                   "  message='" + messageBody + "'\n" +
                   "  shouldForward=" + shouldForward + "\n" +
                   "  reasons='" + filterReasons.toString().trim() + "'\n" +
                   "}";
        }
    }
} 