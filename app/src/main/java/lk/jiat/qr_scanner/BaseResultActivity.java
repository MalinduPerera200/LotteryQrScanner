package lk.jiat.qr_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class BaseResultActivity extends AppCompatActivity {

    private static final String TAG = "BaseResultActivity";

    protected FirebaseFirestore db;
    protected LottieAnimationView loadingAnimation;
    protected View contentLayout;
    protected Button viewPriceButton;


    @LayoutRes
    protected abstract int getLayoutResourceId();

    protected abstract String getFirestoreCollectionName();

    protected abstract void initializeViews();

    protected abstract void displayAndCompareResults(DocumentSnapshot document, QRParser parser);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        db = FirebaseFirestore.getInstance();

        setupWindowInsets();
        initializeCommonViews();
        initializeViews();

        String qrResult = getIntent().getStringExtra("QrResult");
        if (qrResult != null && !qrResult.trim().isEmpty()) {
            parseAndFetchFromFirestore(qrResult);
        } else {
            showError("No QR result found");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadingAnimation != null && loadingAnimation.isAnimating()) {
            loadingAnimation.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loadingAnimation != null && loadingAnimation.getVisibility() == View.VISIBLE) {
            loadingAnimation.resumeAnimation();
        }
    }
    private void initializeCommonViews() {
        loadingAnimation = findViewById(R.id.loading_animation);
        contentLayout = findViewById(R.id.content_layout);
        viewPriceButton = findViewById(R.id.view_result_button);

        if (loadingAnimation != null) {
            loadingAnimation.setAnimation(R.raw.loading_animation);
            loadingAnimation.setRepeatCount(-1); // Infinite loop
            loadingAnimation.setSpeed(1.0f);
        }
    }

    private void parseAndFetchFromFirestore(String qrResult) {
        showLoading(true);
        final QRParser parser = new QRParser(qrResult);
        if (!parser.isValid()) {
            showError("Could not parse QR Code");
            return;
        }

        long drawNumberValue;
        try {
            drawNumberValue = Long.parseLong(parser.getDrawNumber());
        } catch (NumberFormatException e) {
            showError("Invalid Draw Number in QR Code");
            return;
        }

        db.collection(getFirestoreCollectionName())
                .whereEqualTo("Draw_No", drawNumberValue)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        // දත්ත ලැබුණු පසු, subclass එකේ ඇති display ක්‍රමය call කරයි.
                        displayAndCompareResults(document, parser);
                    } else {
                        Log.e(TAG, "Error fetching document or document not found.", task.getException());
                        showResultNotFound(drawNumberValue);
                    }
                });
    }
    protected void showLoading(boolean isLoading) {
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
    }

    protected void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showResultNotFound(long drawNumber) {
        Toast.makeText(this, "No results found for draw: " + drawNumber, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, ResultNotFoundActivity.class));
        finish();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public static class QRParser {
        private String lotteryName = "";
        private String drawNumber = "";
        private String singleLetter = "";
        private String secondLetter = "";
        private String fiveDigitNumber = "";
        private final List<String> winningNumbers = new ArrayList<>();
        private boolean valid = false;

        public QRParser(String qrResult) {
            if (qrResult != null && !qrResult.isEmpty()) {
                parse(qrResult.trim());
            }
        }

        private void parse(String inputString) {
            String[] parts = inputString.split("\\s+");
            StringBuilder lotteryNameBuilder = new StringBuilder();

            Pattern drawNoPattern = Pattern.compile("\\d{4,}");
            Pattern letterPattern = Pattern.compile("[A-Z]");
            Pattern numberPattern = Pattern.compile("\\d{1,2}");
            Pattern fiveDigitPattern = Pattern.compile("\\d{5}");

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
                    } else if (numberPattern.matcher(part).matches()) {
                        winningNumbers.add(part);
                    }
                    else if (!winningNumbers.isEmpty() && secondLetter.isEmpty() && letterPattern.matcher(part).matches() && part.length() == 1) {
                        secondLetter = part;
                    }
                    else if (fiveDigitPattern.matcher(part).matches()) {
                        fiveDigitNumber = part;
                    }
                }
            }

            lotteryName = lotteryNameBuilder.toString().trim();
            // Update validation to ensure it's a valid QR
            valid = !lotteryName.isEmpty() && !drawNumber.isEmpty() && !winningNumbers.isEmpty();
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getLotteryName() { return lotteryName; }
        public String getDrawNumber() { return drawNumber; }
        public String getSingleLetter() { return singleLetter; }
        public List<String> getWinningNumbers() { return winningNumbers; }
        public String getSecondLetter() { return secondLetter; }
        public String getFiveDigitNumber() { return fiveDigitNumber; }
    }
}