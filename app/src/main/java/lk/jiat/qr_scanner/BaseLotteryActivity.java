package lk.jiat.qr_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

import lk.jiat.qr_scanner.constants.LotteryConstants;
import lk.jiat.qr_scanner.error.ErrorHandler;
import lk.jiat.qr_scanner.validation.QRValidator;

/**
 * Updated base class for all lottery result activities with comprehensive error handling
 * and input validation using the new error handling and validation systems.
 */
public abstract class BaseLotteryActivity extends AppCompatActivity {

    private static final String TAG = "BaseLotteryActivity";

    // Common UI components
    protected FirebaseFirestore db;
    protected LottieAnimationView loadingAnimation;
    protected View contentLayout;
    protected Button viewPriceButton;
    protected TextView lotteryNameTextView, dateTextView, drawNoTextView;
    protected ImageView logoImageView;

    // Data holders
    protected String qrResult;
    protected QRParser qrParser;

    // Abstract methods that subclasses must implement
    @LayoutRes
    protected abstract int getLayoutResourceId();
    protected abstract String getFirestoreCollectionName();
    protected abstract void initializeSpecificViews();
    protected abstract void displayLotteryResults(DocumentSnapshot document, QRParser parser);
    protected abstract MatchResults getMatchResults();
    protected abstract String getLotteryDisplayName();
    protected abstract int getLogoResourceId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        try {
            initializeCommonComponents();
            initializeCommonViews();
            initializeSpecificViews();
            processQRIntent();
        } catch (Exception e) {
            ErrorHandler.Handler.handleError(this, e, error -> finish());
        }
    }

    /**
     * Initialize common components like Firebase with error handling
     */
    private void initializeCommonComponents() throws ErrorHandler.FirestoreException {
        try {
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            throw new ErrorHandler.FirestoreException(e);
        }
    }

    /**
     * Initialize common UI views present in all lottery layouts
     */
    private void initializeCommonViews() {
        setupWindowInsets();

        loadingAnimation = findViewById(R.id.loading_animation);
        contentLayout = findViewById(R.id.content_layout);
        viewPriceButton = findViewById(R.id.view_result_button);
        lotteryNameTextView = findViewById(R.id.lottery_name);
        dateTextView = findViewById(R.id.date);
        drawNoTextView = findViewById(R.id.draw_no);
        logoImageView = findViewById(R.id.logo_image_view);

        setupLoadingAnimation();
        setupViewPriceButton();
        setupLotteryInfo();
    }

    /**
     * Setup Lottie loading animation with constants
     */
    private void setupLoadingAnimation() {
        if (loadingAnimation != null) {
            loadingAnimation.setAnimation(R.raw.loading_animation);
            loadingAnimation.setRepeatCount(LotteryConstants.UI.LOADING_ANIMATION_REPEAT_COUNT);
            loadingAnimation.setSpeed(LotteryConstants.UI.LOADING_ANIMATION_SPEED);
        }
    }

    /**
     * Setup view price button with common click handling
     */
    private void setupViewPriceButton() {
        if (viewPriceButton != null) {
            viewPriceButton.setOnClickListener(v -> {
                try {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click_animation));
                    Intent intent = new Intent(this, ViewPriceActivity.class);
                    MatchResults matchResults = getMatchResults();
                    if (matchResults != null) {
                        matchResults.addToIntent(intent, getLotteryDisplayName());
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    ErrorHandler.Handler.handleError(this, e);
                }
            });
        }
    }

    /**
     * Setup common lottery info (name and logo)
     */
    private void setupLotteryInfo() {
        if (lotteryNameTextView != null) {
            lotteryNameTextView.setText(getLotteryDisplayName());
        }
        if (logoImageView != null) {
            logoImageView.setImageResource(getLogoResourceId());
        }
    }

    /**
     * Process QR code from intent with validation
     */
    private void processQRIntent() {
        qrResult = getIntent().getStringExtra(LotteryConstants.IntentExtras.QR_RESULT);

        try {
            // Validate QR result using our validation system
            ErrorHandler.Handler.validateQRResult(qrResult);
            String sanitizedQR = QRValidator.getSanitizedQR(qrResult);
            parseAndFetchFromFirestore(sanitizedQR);
        } catch (ErrorHandler.QRParsingException e) {
            ErrorHandler.Handler.handleError(this, e, error -> finish());
        } catch (ErrorHandler.ValidationException e) {
            ErrorHandler.Handler.handleError(this, e, error -> finish());
        }
    }

    /**
     * Parse QR code and fetch data from Firestore with comprehensive error handling
     */
    private void parseAndFetchFromFirestore(String qrResult) {
        showLoading(true);

        try {
            qrParser = new QRParser(qrResult);
            if (!qrParser.isValid()) {
                throw new ErrorHandler.QRParsingException("Could not parse QR Code");
            }

            // Validate lottery type is supported
            String lotteryName = qrParser.getLotteryName();
            if (!LotteryConstants.LotteryTypes.MEGA_POWER.equals(lotteryName) &&
                    !LotteryConstants.LotteryTypes.NLB_JAYA.equals(lotteryName) &&
                    !LotteryConstants.LotteryTypes.GOVI_SETHA.equals(lotteryName) &&
                    !LotteryConstants.LotteryTypes.DHANA_NIDHANAYA.equals(lotteryName) &&
                    !LotteryConstants.LotteryTypes.MAHAJANA_SAMPATHA.equals(lotteryName)) {
                throw new ErrorHandler.UnsupportedLotteryTypeException(lotteryName);
            }

            // Validate and parse draw number
            long drawNumberValue = ErrorHandler.Handler.validateDrawNumber(qrParser.getDrawNumber());
            fetchFromFirestore(drawNumberValue);

        } catch (ErrorHandler.LotteryException e) {
            showLoading(false);
            ErrorHandler.Handler.handleError(this, e, error -> finish());
        }
    }

    /**
     * Fetch lottery data from Firestore with improved error handling
     */
    private void fetchFromFirestore(long drawNumber) {
        db.collection(getFirestoreCollectionName())
                .whereEqualTo(LotteryConstants.FirestoreFields.DRAW_NO, drawNumber)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        updateCommonDisplay(document);
                        displayLotteryResults(document, qrParser);
                    } else {
                        ErrorHandler.Handler.handleError(this,
                                new ErrorHandler.DrawNotFoundException(drawNumber),
                                error -> showResultNotFound(drawNumber));
                    }
                })
                .addOnFailureListener(exception -> {
                    showLoading(false);
                    Log.e(TAG, "Firestore query failed", exception);
                    ErrorHandler.Handler.handleError(this,
                            new ErrorHandler.FirestoreException(exception),
                            error -> finish());
                });
    }

    /**
     * Update common display elements (date, draw number) with error handling
     */
    private void updateCommonDisplay(DocumentSnapshot document) {
        try {
            Timestamp drawDate = document.getTimestamp(LotteryConstants.FirestoreFields.DATE);
            Long drawNo = document.getLong(LotteryConstants.FirestoreFields.DRAW_NO);

            if (dateTextView != null && drawDate != null) {
                dateTextView.setText(new SimpleDateFormat(LotteryConstants.DateFormats.DISPLAY_FORMAT, Locale.getDefault())
                        .format(drawDate.toDate()));
            }

            if (drawNoTextView != null && drawNo != null) {
                drawNoTextView.setText(getString(R.string.draw_number_prefix, drawNo));
            }
        } catch (Exception e) {
            Log.w(TAG, "Error updating common display", e);
            // Don't fail the entire operation for display issues
        }
    }

    /**
     * Show/hide loading animation with proper lifecycle management
     */
    protected void showLoading(boolean isLoading) {
        try {
            if (isLoading) {
                if (contentLayout != null) contentLayout.setVisibility(View.GONE);
                if (loadingAnimation != null) {
                    loadingAnimation.setVisibility(View.VISIBLE);
                    loadingAnimation.playAnimation();
                }
            } else {
                if (contentLayout != null) contentLayout.setVisibility(View.VISIBLE);
                if (loadingAnimation != null) {
                    loadingAnimation.setVisibility(View.GONE);
                    loadingAnimation.cancelAnimation();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error managing loading state", e);
        }
    }

    /**
     * Handle result not found scenario with proper error handling
     */
    private void showResultNotFound(long drawNumber) {
        try {
            String message = getString(R.string.error_draw_not_found) + " " + drawNumber;
            Toast.makeText(this, message, LotteryConstants.UI.TOAST_DURATION_ERROR).show();
            startActivity(new Intent(this, ResultNotFoundActivity.class));
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error showing result not found", e);
            finish();
        }
    }

    /**
     * Setup window insets for edge-to-edge display
     */
    private void setupWindowInsets() {
        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            Log.w(TAG, "Error setting up window insets", e);
        }
    }

    /**
     * Utility method to set match indicator images with error handling
     */
    protected void setMatchImage(boolean isMatch, ImageView... imageViews) {
        try {
            int imageRes = isMatch ? R.drawable.done : R.drawable.close;
            for (ImageView imageView : imageViews) {
                if (imageView != null) {
                    imageView.setImageResource(imageRes);
                    imageView.setVisibility(View.VISIBLE);
                    // Set content description for accessibility
                    imageView.setContentDescription(getString(
                            isMatch ? R.string.content_description_match_tick : R.string.content_description_match_cross
                    ));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error setting match images", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (loadingAnimation != null && loadingAnimation.isAnimating()) {
                loadingAnimation.pauseAnimation();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error pausing animation", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (loadingAnimation != null && loadingAnimation.getVisibility() == View.VISIBLE) {
                loadingAnimation.resumeAnimation();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error resuming animation", e);
        }
    }

    /**
     * Base class for match results that all lottery types should extend
     */
    public abstract static class MatchResults {
        public abstract void addToIntent(Intent intent, String lotteryName);
    }

    /**
     * Enhanced QR Parser with better validation and error handling
     */
    public static class QRParser {
        private String lotteryName = "";
        private String drawNumber = "";
        private String singleLetter = "";
        private String secondLetter = "";
        private String fiveDigitNumber = "";
        private String sixDigitNumber = "";
        private final java.util.List<String> winningNumbers = new java.util.ArrayList<>();
        private boolean valid = false;

        public QRParser(String qrResult) throws ErrorHandler.QRParsingException {
            if (qrResult != null && !qrResult.isEmpty()) {
                parse(qrResult.trim());
            } else {
                throw new ErrorHandler.QRParsingException("QR result is null or empty");
            }
        }

        private void parse(String inputString) throws ErrorHandler.QRParsingException {
            try {
                // Enhanced parsing logic with better validation
                String[] parts = inputString.split("\\s+");

                if (parts.length < 3) {
                    throw new ErrorHandler.QRParsingException("QR data has insufficient parts");
                }

                StringBuilder lotteryNameBuilder = new StringBuilder();

                java.util.regex.Pattern drawNoPattern = java.util.regex.Pattern.compile("\\d{4,}");
                java.util.regex.Pattern letterPattern = java.util.regex.Pattern.compile("[A-Z]");
                java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\d{1,2}");
                java.util.regex.Pattern fiveDigitPattern = java.util.regex.Pattern.compile("\\d{5}");
                java.util.regex.Pattern sixDigitPattern = java.util.regex.Pattern.compile("\\d{6}");

                boolean drawNoFound = false;
                for (String part : parts) {
                    if (!drawNoFound && drawNoPattern.matcher(part).matches()) {
                        drawNumber = part;
                        drawNoFound = true;
                        continue;
                    }

                    if (!drawNoFound) {
                        lotteryNameBuilder.append(part).append(" ");
                    } else {
                        if (singleLetter.isEmpty() && letterPattern.matcher(part).matches() && part.length() == 1) {
                            singleLetter = part;
                        } else if (sixDigitPattern.matcher(part).matches()) {
                            sixDigitNumber = part;
                        } else if (fiveDigitPattern.matcher(part).matches()) {
                            fiveDigitNumber = part;
                        } else if (numberPattern.matcher(part).matches()) {
                            winningNumbers.add(part);
                        } else if (!winningNumbers.isEmpty() && secondLetter.isEmpty() && letterPattern.matcher(part).matches() && part.length() == 1) {
                            secondLetter = part;
                        }
                    }
                }

                lotteryName = lotteryNameBuilder.toString().trim();
                valid = !lotteryName.isEmpty() && !drawNumber.isEmpty();

                if (!valid) {
                    throw new ErrorHandler.QRParsingException("Invalid QR structure: missing lottery name or draw number");
                }
            } catch (Exception e) {
                if (e instanceof ErrorHandler.QRParsingException) {
                    throw e;
                }
                throw new ErrorHandler.QRParsingException("Failed to parse QR data: " + e.getMessage());
            }
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getLotteryName() { return lotteryName; }
        public String getDrawNumber() { return drawNumber; }
        public String getSingleLetter() { return singleLetter; }
        public String getSecondLetter() { return secondLetter; }
        public String getFiveDigitNumber() { return fiveDigitNumber; }
        public String getSixDigitNumber() { return sixDigitNumber; }
        public java.util.List<String> getWinningNumbers() { return winningNumbers; }
    }
}