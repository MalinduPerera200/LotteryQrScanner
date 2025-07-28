package lk.jiat.qr_scanner.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import lk.jiat.qr_scanner.error.ErrorHandler;

/**
 * Comprehensive QR validation and sanitization system
 */
public class QRValidator {

    // Constants for validation
    private static final int MAX_QR_LENGTH = 500;
    private static final int MIN_QR_LENGTH = 10;
    private static final int MIN_DRAW_NUMBER_LENGTH = 4;
    private static final int MAX_DRAW_NUMBER_LENGTH = 8;

    // Supported lottery types
    private static final Set<String> SUPPORTED_LOTTERIES = new HashSet<>(Arrays.asList(
            "Mega Power",
            "NLB Jaya",
            "Govi Setha",
            "Dhana Nidhanaya",
            "Mahajana Sampatha"
    ));

    // Regex patterns for validation
    private static final Pattern DRAW_NUMBER_PATTERN = Pattern.compile("\\d{4,8}");
    private static final Pattern LETTER_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d{1,2}");
    private static final Pattern FIVE_DIGIT_PATTERN = Pattern.compile("\\d{5}");
    private static final Pattern SIX_DIGIT_PATTERN = Pattern.compile("\\d{6}");

    /**
     * Validation result wrapper
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String sanitizedData;
        private final String errorMessage;

        private ValidationResult(boolean valid, String sanitizedData, String errorMessage) {
            this.valid = valid;
            this.sanitizedData = sanitizedData;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success(String sanitizedData) {
            return new ValidationResult(true, sanitizedData, null);
        }

        public static ValidationResult error(String errorMessage) {
            return new ValidationResult(false, null, errorMessage);
        }

        public boolean isValid() { return valid; }
        public String getSanitizedData() { return sanitizedData; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Detailed QR structure for validation
     */
    public static class QRStructure {
        private final String lotteryName;
        private final String drawNumber;
        private final String[] components;
        private final boolean valid;

        public QRStructure(String lotteryName, String drawNumber, String[] components, boolean valid) {
            this.lotteryName = lotteryName;
            this.drawNumber = drawNumber;
            this.components = components;
            this.valid = valid;
        }

        public String getLotteryName() { return lotteryName; }
        public String getDrawNumber() { return drawNumber; }
        public String[] getComponents() { return components; }
        public boolean isValid() { return valid; }
    }

    /**
     * Main validation method
     */
    public static ValidationResult validate(String qrData) {
        try {
            // Step 1: Basic sanitization
            String sanitized = sanitizeInput(qrData);

            // Step 2: Length validation
            if (!isValidLength(sanitized)) {
                return ValidationResult.error("QR code length is invalid");
            }

            // Step 3: Format validation
            if (!isValidFormat(sanitized)) {
                return ValidationResult.error("QR code format is invalid");
            }

            // Step 4: Structure validation
            QRStructure structure = parseStructure(sanitized);
            if (!structure.isValid()) {
                return ValidationResult.error("QR code structure is invalid");
            }

            // Step 5: Content validation
            if (!validateContent(structure)) {
                return ValidationResult.error("QR code content is invalid");
            }

            return ValidationResult.success(sanitized);

        } catch (Exception e) {
            return ValidationResult.error("QR validation failed: " + e.getMessage());
        }
    }

    /**
     * Sanitize input data
     */
    private static String sanitizeInput(String input) throws ErrorHandler.ValidationException {
        if (input == null) {
            throw new ErrorHandler.ValidationException("QR data is null");
        }

        // Remove leading/trailing whitespace
        String sanitized = input.trim();

        // Remove any non-printable characters except spaces
        sanitized = sanitized.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // Normalize multiple spaces to single space
        sanitized = sanitized.replaceAll("\\s+", " ");

        // Remove any potentially dangerous characters
        sanitized = sanitized.replaceAll("[<>\"'&]", "");

        return sanitized;
    }

    /**
     * Validate length constraints
     */
    private static boolean isValidLength(String data) {
        return data.length() >= MIN_QR_LENGTH && data.length() <= MAX_QR_LENGTH;
    }

    /**
     * Validate basic format
     */
    private static boolean isValidFormat(String data) {
        // Should contain at least letters, numbers, and spaces
        return data.matches(".*[A-Za-z].*") && data.matches(".*\\d.*");
    }

    /**
     * Parse and validate QR structure
     */
    private static QRStructure parseStructure(String data) {
        String[] parts = data.split("\\s+");

        if (parts.length < 3) {
            return new QRStructure(null, null, parts, false);
        }

        StringBuilder lotteryNameBuilder = new StringBuilder();
        String drawNumber = null;
        boolean drawNumberFound = false;

        // Find draw number to separate lottery name from data
        for (String part : parts) {
            if (!drawNumberFound && DRAW_NUMBER_PATTERN.matcher(part).matches()) {
                drawNumber = part;
                drawNumberFound = true;
                break;
            }
            if (!drawNumberFound) {
                lotteryNameBuilder.append(part).append(" ");
            }
        }

        String lotteryName = lotteryNameBuilder.toString().trim();

        if (!drawNumberFound || lotteryName.isEmpty()) {
            return new QRStructure(lotteryName, drawNumber, parts, false);
        }

        return new QRStructure(lotteryName, drawNumber, parts, true);
    }

    /**
     * Validate content based on lottery rules
     */
    private static boolean validateContent(QRStructure structure) {
        // Check if lottery type is supported
        if (!SUPPORTED_LOTTERIES.contains(structure.getLotteryName())) {
            return false;
        }

        // Validate draw number
        if (!isValidDrawNumber(structure.getDrawNumber())) {
            return false;
        }

        // Validate lottery-specific content
        return validateLotterySpecificContent(structure);
    }

    /**
     * Validate draw number format and range
     */
    private static boolean isValidDrawNumber(String drawNumber) {
        if (drawNumber == null || drawNumber.length() < MIN_DRAW_NUMBER_LENGTH ||
                drawNumber.length() > MAX_DRAW_NUMBER_LENGTH) {
            return false;
        }

        try {
            long number = Long.parseLong(drawNumber);
            // Reasonable range for draw numbers (adjust as needed)
            return number > 0 && number < 99999999L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate lottery-specific content rules
     */
    private static boolean validateLotterySpecificContent(QRStructure structure) {
        String lotteryName = structure.getLotteryName();
        String[] parts = structure.getComponents();

        switch (lotteryName) {
            case "Mega Power":
                return validateMegaPowerContent(parts);
            case "NLB Jaya":
                return validateNLBJayaContent(parts);
            case "Govi Setha":
                return validateGoviSethaContent(parts);
            case "Dhana Nidhanaya":
                return validateDhanaNidhanayaContent(parts);
            case "Mahajana Sampatha":
                return validateMahajanaSampathaContent(parts);
            default:
                return false;
        }
    }

    /**
     * Validate Mega Power specific content
     */
    private static boolean validateMegaPowerContent(String[] parts) {
        int letterCount = 0;
        int numberCount = 0;

        for (String part : parts) {
            if (LETTER_PATTERN.matcher(part).matches() && part.length() == 1) {
                letterCount++;
            } else if (NUMBER_PATTERN.matcher(part).matches()) {
                numberCount++;
            }
        }

        // Mega Power should have: 1 letter + 5 numbers (including bonus)
        return letterCount >= 1 && numberCount >= 4;
    }

    /**
     * Validate NLB Jaya specific content
     */
    private static boolean validateNLBJayaContent(String[] parts) {
        int letterCount = 0;
        int numberCount = 0;

        for (String part : parts) {
            if (LETTER_PATTERN.matcher(part).matches() && part.length() == 1) {
                letterCount++;
            } else if (NUMBER_PATTERN.matcher(part).matches()) {
                numberCount++;
            }
        }

        // NLB Jaya should have: 1 letter + 4 numbers
        return letterCount >= 1 && numberCount >= 4;
    }

    /**
     * Validate Govi Setha specific content
     */
    private static boolean validateGoviSethaContent(String[] parts) {
        int letterCount = 0;
        int numberCount = 0;

        for (String part : parts) {
            if (LETTER_PATTERN.matcher(part).matches() && part.length() == 1) {
                letterCount++;
            } else if (NUMBER_PATTERN.matcher(part).matches()) {
                numberCount++;
            }
        }

        // Govi Setha should have: 2 letters + 4 numbers
        return letterCount >= 2 && numberCount >= 4;
    }

    /**
     * Validate Dhana Nidhanaya specific content
     */
    private static boolean validateDhanaNidhanayaContent(String[] parts) {
        int letterCount = 0;
        int numberCount = 0;
        boolean hasFiveDigit = false;

        for (String part : parts) {
            if (LETTER_PATTERN.matcher(part).matches() && part.length() == 1) {
                letterCount++;
            } else if (NUMBER_PATTERN.matcher(part).matches()) {
                numberCount++;
            } else if (FIVE_DIGIT_PATTERN.matcher(part).matches()) {
                hasFiveDigit = true;
            }
        }

        // Dhana Nidhanaya should have: 2 letters + 4 numbers + 1 five-digit special number
        return letterCount >= 2 && numberCount >= 4 && hasFiveDigit;
    }

    /**
     * Validate Mahajana Sampatha specific content
     */
    private static boolean validateMahajanaSampathaContent(String[] parts) {
        int letterCount = 0;
        boolean hasSixDigit = false;

        for (String part : parts) {
            if (LETTER_PATTERN.matcher(part).matches() && part.length() == 1) {
                letterCount++;
            } else if (SIX_DIGIT_PATTERN.matcher(part).matches()) {
                hasSixDigit = true;
            }
        }

        // Mahajana Sampatha should have: 1 letter + 1 six-digit number
        return letterCount >= 1 && hasSixDigit;
    }

    /**
     * Quick validation for simple checks
     */
    public static boolean isValidQR(String qrData) {
        ValidationResult result = validate(qrData);
        return result.isValid();
    }

    /**
     * Get sanitized QR data
     */
    public static String getSanitizedQR(String qrData) throws ErrorHandler.ValidationException {
        ValidationResult result = validate(qrData);
        if (!result.isValid()) {
            throw new ErrorHandler.ValidationException(result.getErrorMessage());
        }
        return result.getSanitizedData();
    }
}