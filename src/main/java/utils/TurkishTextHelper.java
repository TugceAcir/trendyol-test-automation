package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * TurkishTextHelper - Utilities for handling Turkish text
 * 
 * WHY THIS CLASS EXISTS:
 * - Trendyol.com is a Turkish website
 * - Turkish has special characters: ç, ğ, ı, ö, ş, ü, Ç, Ğ, İ, Ö, Ş, Ü
 * - Turkish uppercase/lowercase rules are different (i → İ, I → ı)
 * - Search queries, text validation need proper Turkish handling
 * 
 * WHAT IT PROVIDES:
 * - Turkish character validation
 * - Proper uppercase/lowercase conversion
 * - ASCII normalization (for comparison)
 * - Turkish-aware text comparison
 */
public class TurkishTextHelper {
    
    private static final Logger logger = LogManager.getLogger(TurkishTextHelper.class);
    
    // Turkish special characters
    private static final String TURKISH_LOWER = "çğıöşü";
    private static final String TURKISH_UPPER = "ÇĞİÖŞÜ";
    
    // Turkish to ASCII mapping for normalization
    private static final Map<Character, Character> TURKISH_TO_ASCII = new HashMap<>();
    
    static {
        TURKISH_TO_ASCII.put('ç', 'c');
        TURKISH_TO_ASCII.put('Ç', 'C');
        TURKISH_TO_ASCII.put('ğ', 'g');
        TURKISH_TO_ASCII.put('Ğ', 'G');
        TURKISH_TO_ASCII.put('ı', 'i');
        TURKISH_TO_ASCII.put('İ', 'I');
        TURKISH_TO_ASCII.put('ö', 'o');
        TURKISH_TO_ASCII.put('Ö', 'O');
        TURKISH_TO_ASCII.put('ş', 's');
        TURKISH_TO_ASCII.put('Ş', 'S');
        TURKISH_TO_ASCII.put('ü', 'u');
        TURKISH_TO_ASCII.put('Ü', 'U');
    }
    
    /**
     * Check if text contains Turkish characters
     * 
     * USE CASE: Validate Turkish content on Trendyol pages
     * 
     * @param text - text to check
     * @return true if contains Turkish characters
     */
    public static boolean containsTurkishCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (char c : text.toCharArray()) {
            if (TURKISH_LOWER.indexOf(c) >= 0 || TURKISH_UPPER.indexOf(c) >= 0) {
                logger.debug("Turkish character found: {}", c);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Convert to uppercase using Turkish locale rules
     * 
     * CRITICAL: Turkish has different uppercase rules!
     * - Standard: i → I (wrong for Turkish)
     * - Turkish:  i → İ, ı → I
     * 
     * EXAMPLE:
     * - "istanbul" → "İSTANBUL" (Turkish correct)
     * - "istanbul" → "ISTANBUL" (English wrong)
     * 
     * @param text - text to convert
     * @return uppercase text (Turkish rules)
     */
    public static String toUpperCaseTurkish(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        logger.debug("Converting to uppercase (Turkish): '{}'", text);
        
        // Use Turkish locale for proper conversion
        String result = text.toUpperCase(new java.util.Locale("tr", "TR"));
        
        logger.debug("Result: '{}'", result);
        return result;
    }
    
    /**
     * Convert to lowercase using Turkish locale rules
     * 
     * CRITICAL: Turkish has different lowercase rules!
     * - Standard: I → i (wrong for Turkish)
     * - Turkish:  İ → i, I → ı
     * 
     * @param text - text to convert
     * @return lowercase text (Turkish rules)
     */
    public static String toLowerCaseTurkish(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        logger.debug("Converting to lowercase (Turkish): '{}'", text);
        
        // Use Turkish locale for proper conversion
        String result = text.toLowerCase(new java.util.Locale("tr", "TR"));
        
        logger.debug("Result: '{}'", result);
        return result;
    }
    
    /**
     * Normalize Turkish text to ASCII (remove diacritics)
     * 
     * USE CASE: Compare text regardless of Turkish characters
     * EXAMPLE: "Çiçek" → "Cicek", "Şehir" → "Sehir"
     * 
     * WARNING: Information loss! Use only for comparison, not display
     * 
     * @param text - text to normalize
     * @return ASCII-normalized text
     */
    public static String normalizeToAscii(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        logger.debug("Normalizing to ASCII: '{}'", text);
        
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if (TURKISH_TO_ASCII.containsKey(c)) {
                result.append(TURKISH_TO_ASCII.get(c));
            } else {
                result.append(c);
            }
        }
        
        // Also handle other diacritics (é, ñ, etc.)
        String normalized = Normalizer.normalize(result.toString(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        
        logger.debug("Normalized result: '{}'", normalized);
        return normalized;
    }
    
    /**
     * Case-insensitive Turkish comparison
     * 
     * USE CASE: Compare text on Trendyol ignoring case differences
     * EXAMPLE: "istanbul" equals "İSTANBUL" (Turkish context)
     * 
     * @param text1 - first text
     * @param text2 - second text
     * @return true if equal (case-insensitive, Turkish rules)
     */
    public static boolean equalsIgnoreCaseTurkish(String text1, String text2) {
        if (text1 == null && text2 == null) {
            return true;
        }
        if (text1 == null || text2 == null) {
            return false;
        }
        
        String normalized1 = toLowerCaseTurkish(text1);
        String normalized2 = toLowerCaseTurkish(text2);
        
        boolean result = normalized1.equals(normalized2);
        
        logger.debug("Turkish case-insensitive comparison: '{}' vs '{}' = {}", text1, text2, result);
        return result;
    }
    
    /**
     * Turkish-aware contains check (case-insensitive)
     * 
     * USE CASE: Check if product name contains search keyword
     * 
     * @param text - text to search in
     * @param substring - substring to find
     * @return true if text contains substring (case-insensitive, Turkish)
     */
    public static boolean containsIgnoreCaseTurkish(String text, String substring) {
        if (text == null || substring == null) {
            return false;
        }
        
        String normalizedText = toLowerCaseTurkish(text);
        String normalizedSubstring = toLowerCaseTurkish(substring);
        
        boolean result = normalizedText.contains(normalizedSubstring);
        
        logger.debug("Turkish contains check: '{}' contains '{}' = {}", text, substring, result);
        return result;
    }
    
    /**
     * Fuzzy Turkish comparison (ignores diacritics)
     * 
     * USE CASE: When you want "Cicek" to match "Çiçek"
     * EXAMPLE: Search suggestions, typo tolerance
     * 
     * @param text1 - first text
     * @param text2 - second text
     * @return true if equal after ASCII normalization
     */
    public static boolean fuzzyEqualsTurkish(String text1, String text2) {
        if (text1 == null && text2 == null) {
            return true;
        }
        if (text1 == null || text2 == null) {
            return false;
        }
        
        String normalized1 = normalizeToAscii(toLowerCaseTurkish(text1));
        String normalized2 = normalizeToAscii(toLowerCaseTurkish(text2));
        
        boolean result = normalized1.equals(normalized2);
        
        logger.debug("Turkish fuzzy comparison: '{}' vs '{}' = {}", text1, text2, result);
        return result;
    }
    
    /**
     * Remove extra whitespace (Turkish-aware trim)
     * 
     * USE CASE: Clean user input, text scraped from web
     * 
     * @param text - text to clean
     * @return cleaned text
     */
    public static String cleanWhitespace(String text) {
        if (text == null) {
            return null;
        }
        
        // Trim and replace multiple spaces with single space
        String cleaned = text.trim().replaceAll("\\s+", " ");
        
        logger.debug("Cleaned whitespace: '{}' → '{}'", text, cleaned);
        return cleaned;
    }
    
    /**
     * Validate Turkish text (only Turkish chars, spaces, numbers allowed)
     * 
     * USE CASE: Validate Turkish names, addresses
     * 
     * @param text - text to validate
     * @return true if valid Turkish text
     */
    public static boolean isValidTurkishText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Allow Turkish letters, spaces, and numbers
        String pattern = "^[a-zA-ZçÇğĞıİöÖşŞüÜ0-9\\s]+$";
        boolean isValid = text.matches(pattern);
        
        logger.debug("Turkish text validation: '{}' = {}", text, isValid);
        return isValid;
    }
    
    /**
     * Check if string starts with Turkish character
     * 
     * @param text - text to check
     * @return true if starts with Turkish character
     */
    public static boolean startsWithTurkishCharacter(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        char firstChar = text.charAt(0);
        return TURKISH_LOWER.indexOf(firstChar) >= 0 || TURKISH_UPPER.indexOf(firstChar) >= 0;
    }
    
    /**
     * Get Turkish character count in text
     * 
     * @param text - text to analyze
     * @return count of Turkish characters
     */
    public static int getTurkishCharacterCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (char c : text.toCharArray()) {
            if (TURKISH_LOWER.indexOf(c) >= 0 || TURKISH_UPPER.indexOf(c) >= 0) {
                count++;
            }
        }
        
        logger.debug("Turkish character count in '{}': {}", text, count);
        return count;
    }
    
    /**
     * Replace Turkish characters with English equivalents
     * 
     * USE CASE: Create URL-friendly slugs, file names
     * EXAMPLE: "Çiçek Mağazası" → "Cicek Magazasi"
     * 
     * @param text - text to convert
     * @return text with Turkish chars replaced
     */
    public static String replaceTurkishChars(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        logger.debug("Replacing Turkish characters: '{}'", text);
        String result = normalizeToAscii(text);
        logger.debug("Result: '{}'", result);
        return result;
    }
    
    /**
     * Format Turkish currency
     * 
     * USE CASE: Format prices on Trendyol (Turkish Lira - TL)
     * EXAMPLE: 1234.56 → "1.234,56 TL"
     * 
     * @param amount - numeric amount
     * @return formatted Turkish currency string
     */
    public static String formatTurkishCurrency(double amount) {
        // Turkish format: thousands separator = ".", decimal separator = ","
        String formatted = String.format(new java.util.Locale("tr", "TR"), "%,.2f TL", amount);
        logger.debug("Formatted currency: {} → {}", amount, formatted);
        return formatted;
    }
    
    /**
     * Parse Turkish formatted number
     * 
     * USE CASE: Parse prices from Trendyol (remove TL, convert to double)
     * EXAMPLE: "1.234,56 TL" → 1234.56
     * 
     * @param formattedNumber - formatted Turkish number string
     * @return parsed double value
     */
    public static double parseTurkishNumber(String formattedNumber) {
        if (formattedNumber == null || formattedNumber.isEmpty()) {
            return 0.0;
        }
        
        try {
            // Remove "TL", spaces, and currency symbols
            String cleaned = formattedNumber
                    .replace("TL", "")
                    .replace("₺", "")
                    .replace(" ", "")
                    .trim();
            
            // Replace Turkish decimal separator
            cleaned = cleaned.replace(".", "").replace(",", ".");
            
            double result = Double.parseDouble(cleaned);
            logger.debug("Parsed Turkish number: '{}' → {}", formattedNumber, result);
            return result;
            
        } catch (NumberFormatException e) {
            logger.error("Error parsing Turkish number: '{}'", formattedNumber, e);
            return 0.0;
        }
    }
}
