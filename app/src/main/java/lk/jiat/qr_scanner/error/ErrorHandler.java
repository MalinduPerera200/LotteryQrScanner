package lk.jiat.qr_scanner.error;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.StringRes;

import lk.jiat.qr_scanner.R;

/**
 * Comprehensive error handling system for the lottery QR scanner app.
 * Provides specific error types and user-friendly error messages.
 */
public class ErrorHandler {

    /**
     * Custom exception types for different error scenarios
     */
    public static abstract class LotteryException extends Exception {
        @StringRes
        protected int userMessageRes;

        public LotteryException(String message, @StringRes int userMessageRes) {
            super(message);
            this.userMessageRes = userMessageRes;
        }

        public LotteryException(String message, Throwable cause, @StringRes int userMessageRes) {
            super(message, cause);
            this.userMessageRes = userMessageRes;
        }

        @StringRes
        public int getUserMessageRes() {
            return userMessageRes;
        }
    }

    /**
     * QR Code parsing related errors
     */
    public static class QRParsingException extends LotteryException {
        public QRParsingException(String message) {
            super(message, R.string.error_invalid_qr_code);
        }
    }

    public static class InvalidDrawNumberException extends LotteryException {
        private final String drawNumber;

        public InvalidDrawNumberException(String drawNumber) {
            super("Invalid draw number: " + drawNumber, R.string.error_invalid_draw_number);
            this.drawNumber = drawNumber;
        }

        public String getDrawNumber() {
            return drawNumber;
        }
    }

    public static class UnsupportedLotteryTypeException extends LotteryException {
        private final String lotteryType;

        public UnsupportedLotteryTypeException(String lotteryType) {
            super("Unsupported lottery type: " + lotteryType, R.string.error_unsupported_lottery);
            this.lotteryType = lotteryType;
        }

        public String getLotteryType() {
            return lotteryType;
        }
    }

    /**
     * Network and data related errors
     */
    public static class NetworkException extends LotteryException {
        public NetworkException(Throwable cause) {
            super("Network error occurred", cause, R.string.error_network);
        }
    }

    public static class FirestoreException extends LotteryException {
        public FirestoreException(Throwable cause) {
            super("Database error occurred", cause, R.string.error_database);
        }
    }

    public static class DrawNotFoundException extends LotteryException {
        private final long drawNumber;

        public DrawNotFoundException(long drawNumber) {
            super("Draw not found: " + drawNumber, R.string.error_draw_not_found);
            this.drawNumber = drawNumber;
        }

        public long getDrawNumber() {
            return drawNumber;
        }
    }

    /**
     * Camera and QR scanning errors
     */
    public static class CameraPermissionException extends LotteryException {
        public CameraPermissionException() {
            super("Camera permission denied", R.string.error_camera_permission);
        }
    }

    public static class CameraException extends LotteryException {
        public CameraException(Throwable cause) {
            super("Camera error occurred", cause, R.string.error_camera);
        }
    }

    public static class QRScanException extends LotteryException {
        public QRScanException(Throwable cause) {
            super("QR scan failed", cause, R.string.error_qr_scan);
        }
    }

    /**
     * Data validation errors
     */
    public static class ValidationException extends LotteryException {
        public ValidationException(String message) {
            super(message, R.string.error_validation);
        }
    }

    /**
     * Error handler utility methods
     */
    public static class Handler {

        /**
         * Handle error with appropriate user message
         */
        public static void handleError(Context context, Throwable error) {
            if (error instanceof LotteryException) {
                LotteryException lotteryError = (LotteryException) error;
                showUserMessage(context, lotteryError.getUserMessageRes());
                logError(error);
            } else {
                // Unknown error - show generic message
                showUserMessage(context, R.string.error_unknown);
                logError(error);
            }
        }

        /**
         * Handle error with custom callback
         */
        public static void handleError(Context context, Throwable error, ErrorCallback callback) {
            handleError(context, error);
            if (callback != null) {
                callback.onErrorHandled(error);
            }
        }

        /**
         * Validate QR result and throw appropriate exception if invalid
         */
        public static void validateQRResult(String qrResult) throws QRParsingException {
            if (qrResult == null || qrResult.trim().isEmpty()) {
                throw new QRParsingException("QR result is null or empty");
            }

            if (qrResult.length() > 1000) { // Reasonable limit
                throw new QRParsingException("QR result is too long");
            }

            // Add more validation as needed
        }

        /**
         * Validate draw number and throw exception if invalid
         */
        public static long validateDrawNumber(String drawNumberStr) throws InvalidDrawNumberException {
            if (drawNumberStr == null || drawNumberStr.trim().isEmpty()) {
                throw new InvalidDrawNumberException(drawNumberStr);
            }

            try {
                long drawNumber = Long.parseLong(drawNumberStr.trim());
                if (drawNumber <= 0) {
                    throw new InvalidDrawNumberException(drawNumberStr);
                }
                return drawNumber;
            } catch (NumberFormatException e) {
                throw new InvalidDrawNumberException(drawNumberStr);
            }
        }

        /**
         * Show user-friendly error message
         */
        private static void showUserMessage(Context context, @StringRes int messageRes) {
            Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show();
        }

        /**
         * Log error for debugging
         */
        private static void logError(Throwable error) {
            android.util.Log.e("LotteryApp", "Error occurred: " + error.getMessage(), error);
            // In production, send to crash reporting service like Firebase Crashlytics
            // FirebaseCrashlytics.getInstance().recordException(error);
        }
    }

    /**
     * Callback interface for error handling
     */
    public interface ErrorCallback {
        void onErrorHandled(Throwable error);
    }

    /**
     * Result wrapper class for operations that might fail
     */
    public static class Result<T> {
        private final T data;
        private final LotteryException error;

        private Result(T data, LotteryException error) {
            this.data = data;
            this.error = error;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(data, null);
        }

        public static <T> Result<T> error(LotteryException error) {
            return new Result<>(null, error);
        }

        public boolean isSuccess() {
            return error == null;
        }

        public boolean isError() {
            return error != null;
        }

        public T getData() {
            return data;
        }

        public LotteryException getError() {
            return error;
        }
    }
}
