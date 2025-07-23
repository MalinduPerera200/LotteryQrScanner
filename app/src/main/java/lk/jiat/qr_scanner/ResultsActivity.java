package lk.jiat.qr_scanner;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "ResultsActivity";

    // UI Components
    private TextView lotteryNameTextView, dateTextView, drawNoTextView;
    private TextView englishLetterTextView, bonusNumTextView,specialNumTextView;
    private TextView englishLetterTextView1, bonusNumTextView1,specialNumTextView1;
    private TextView[] officialNumTextViews = new TextView[4];
    private TextView[] userNumTextViews = new TextView[4];
    private ImageView logoImageView;
    private ImageView[] officialNumImageViews = new ImageView[4];
    private ImageView[] userNumImageViews = new ImageView[4];
    private ImageView englishLetterImOfficial, englishLetterImUser, bonusNumImOfficial, bonusNumImUser;

    // Replace ProgressBar with LottieAnimationView
    private LottieAnimationView loadingAnimation;
    private View contentLayout;
    private Button viewPriceButton;
    private CardView bonusNumberCard, bonusNumberCard1;

    // Data
    private FirebaseFirestore db;
    private List<String> winningNumbers;
    private String drawNumber = "";
    private String singleLetter = "";

    // Match Results
    private MatchResults matchResults = new MatchResults();

    // Lottery Configuration
    private static final Map<String, LotteryConfig> LOTTERY_CONFIGS = new HashMap<String, LotteryConfig>() {{
        put("Mega Power", new LotteryConfig("Mega_Power", R.drawable.mega_logo));
        put("NLB Jaya", new LotteryConfig("NLB_Jaya", R.drawable.nlb_jaya_logo));
        put("Ada Sampatha", new LotteryConfig("Ada_Sampatha", R.drawable.ada_sampatha));
        put("Hadahana", new LotteryConfig("Hadahana", R.drawable.hadahana));
        put("Govi Setha", new LotteryConfig("Govi_Setha", R.drawable.govisetha));
        put("Dhana Nidhanaya", new LotteryConfig("Dhana_Nidhanaya", R.drawable.dana_nidanaya));
        put("Mahajana Sampatha", new LotteryConfig("Mahajana_Sampatha", R.drawable.mahajana_sampatha));
        put("SUBA DAWASA", new LotteryConfig("Suba_Dawasak", R.drawable.suba_dawasak));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        setupWindowInsets();

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupClickListeners();

        String qrResult = getIntent().getStringExtra("QrResult");
        if (qrResult != null && !qrResult.trim().isEmpty()) {
            parseAndFetchResults(qrResult);
        } else {
            showError("No QR result found");
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        // Text Views
        lotteryNameTextView = findViewById(R.id.lottery_name);
        dateTextView = findViewById(R.id.date);
        drawNoTextView = findViewById(R.id.draw_no);
        englishLetterTextView = findViewById(R.id.english_letter);
        bonusNumTextView = findViewById(R.id.bonusNumber);
        englishLetterTextView1 = findViewById(R.id.english_letter_user);
        bonusNumTextView1 = findViewById(R.id.bonus_num_user);
        specialNumTextView = findViewById(R.id.specialNumber);
        specialNumTextView1 = findViewById(R.id.specialNumberUser);

        // Official Numbers
        int[] officialNumIds = {R.id.num_01, R.id.num_02, R.id.num_03, R.id.num_04};
        for (int i = 0; i < 4; i++) {
            officialNumTextViews[i] = findViewById(officialNumIds[i]);
        }

        // User Numbers
        int[] userNumIds = {R.id.num_05, R.id.num_06, R.id.num_07, R.id.num_08};
        for (int i = 0; i < 4; i++) {
            userNumTextViews[i] = findViewById(userNumIds[i]);
        }

        // Image Views
        logoImageView = findViewById(R.id.logo_image_view);
        englishLetterImOfficial = findViewById(R.id.english_letter_indicator2);
        englishLetterImUser = findViewById(R.id.english_letter_indicator);
        bonusNumImOfficial = findViewById(R.id.english_letter_indicator3);
        bonusNumImUser = findViewById(R.id.bonus_num_indicator);

        int[] officialNumImgIds = {R.id.english_letter_indicator4, R.id.english_letter_indicator5,
                R.id.english_letter_indicator6, R.id.english_letter_indicator7};
        int[] userNumImgIds = {R.id.num_05_indicator, R.id.num_06_indicator,
                R.id.num_07_indicator, R.id.num_08_indicator};

        for (int i = 0; i < 4; i++) {
            officialNumImageViews[i] = findViewById(officialNumImgIds[i]);
            userNumImageViews[i] = findViewById(userNumImgIds[i]);
        }

        // Initialize Lottie Animation instead of ProgressBar
        loadingAnimation = findViewById(R.id.loading_animation);
        contentLayout = findViewById(R.id.content_layout);
        viewPriceButton = findViewById(R.id.view_result_button);
        bonusNumberCard = findViewById(R.id.bonusNumberCard);
        bonusNumberCard1 = findViewById(R.id.bonusNumberCard1);

        // Setup Lottie Animation
        setupLottieAnimation();
    }

    private void setupLottieAnimation() {
        loadingAnimation.setAnimation(R.raw.loading_animation);
        loadingAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                Log.d(TAG, "Animation started");
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                Log.d(TAG, "Animation ended");
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                Log.d(TAG, "Animation canceled");
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
                Log.d(TAG, "Animation repeated");
            }
        });

        loadingAnimation.setRepeatCount(-1); // Infinite loop
        loadingAnimation.setSpeed(1.0f); // Normal speed

    }

    private void setupClickListeners() {
        viewPriceButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click_animation));
            Intent intent = new Intent(this, ViewPriceActivity.class);
            matchResults.addToIntent(intent, lotteryNameTextView.getText().toString());
            startActivity(intent);
        });
    }

    private void parseAndFetchResults(String qrResult) {
        showLoading(true);

        QRParser parser = new QRParser(qrResult);
        if (!parser.isValid()) {
            showError("Could not parse QR Code");
            return;
        }

        String lotteryName = parser.getLotteryName();
        drawNumber = parser.getDrawNumber();
        singleLetter = parser.getSingleLetter();
        winningNumbers = parser.getWinningNumbers();

        updateUserDisplay();
        fetchOfficialResults(lotteryName);
    }

    private void updateUserDisplay() {
        englishLetterTextView1.setText(singleLetter);
        boolean hasBonusNumber = winningNumbers.size() >= 5;

        // In updateUserDisplay() - potential IndexOutOfBoundsException
        if (hasBonusNumber) {
            bonusNumTextView1.setText(winningNumbers.get(0));
            for (int i = 0; i < 4; i++) {
                // Need to check if index exists
                if (i + 1 < winningNumbers.size()) {
                    userNumTextViews[i].setText(winningNumbers.get(i + 1));
                }
            }
        } else {
            bonusNumTextView1.setVisibility(View.GONE);
            bonusNumImUser.setVisibility(View.GONE);
            for (int i = 0; i < 4; i++) {
                userNumTextViews[i].setText(winningNumbers.get(i));
            }
        }
    }

    private void fetchOfficialResults(String lotteryName) {
        LotteryConfig config = LOTTERY_CONFIGS.get(lotteryName);
        if (config == null) {
            showError("Unsupported lottery type: " + lotteryName);
            return;
        }

        lotteryNameTextView.setText(lotteryName);
        logoImageView.setImageResource(config.logoResource);

        long drawNumberValue;
        try {
            drawNumberValue = Long.parseLong(drawNumber);
        } catch (NumberFormatException e) {
            showError("Invalid Draw Number in QR Code");
            return;
        }

        db.collection(config.collectionName)
                .whereEqualTo("Draw_No", drawNumberValue)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        displayAndCompareResults(document, lotteryName);
                    } else {
                        showResultNotFound(drawNumberValue);
                    }
                });
    }

    private void displayAndCompareResults(DocumentSnapshot document, String lotteryName) {
        try {
            matchResults.reset();
            currentOfficialResults = new OfficialResults(document);
            currentUserResults = new UserResults(winningNumbers, singleLetter);

            updateOfficialDisplay(currentOfficialResults);
            updateBonusDisplay(currentOfficialResults.bonusNum);
            updateSpecialNumberDisplay(currentOfficialResults);

            // Compare results
            matchResults.isLetterMatched = currentOfficialResults.engLetter != null &&
                    currentOfficialResults.engLetter.equals(currentUserResults.letter);
            matchResults.isBonusMatched = currentOfficialResults.bonusNum != null &&
                    currentUserResults.bonusNum != null &&
                    currentOfficialResults.bonusNum.equals(currentUserResults.bonusNum);

            if (lotteryName.equals("NLB Jaya")) {
                compareNLBJayaNumbers(currentOfficialResults.numbersList, currentUserResults.numbersList);
            } else {
                compareStandardNumbers(currentOfficialResults.numbersList, currentUserResults.numbersList);
            }

            updateMatchIndicators();

        } catch (Exception e) {
            Log.e(TAG, "Error displaying results", e);
        }
    }

    private void updateOfficialDisplay(OfficialResults official) {
        if (official.drawDate != null) {
            dateTextView.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    .format(official.drawDate.toDate()));
        }
        drawNoTextView.setText(official.drawNo != null ? "Draw #" + official.drawNo : "N/A");
        englishLetterTextView.setText(official.engLetter != null ? official.engLetter : "N/A");

        for (int i = 0; i < officialNumTextViews.length; i++) {
            if (i < official.numbersList.size()) {
                officialNumTextViews[i].setText(String.valueOf(official.numbersList.get(i)));
            } else {
                officialNumTextViews[i].setText("N/A");
            }
        }
    }

    private void updateBonusDisplay(Long officialBonusNum) {
        if (officialBonusNum != null) {
            bonusNumTextView.setText(String.valueOf(officialBonusNum));
            setViewsVisibility(View.VISIBLE, bonusNumberCard, bonusNumberCard1,
                    bonusNumTextView, bonusNumImOfficial);
        } else {
            setViewsVisibility(View.GONE, bonusNumTextView, bonusNumberCard,
                    bonusNumberCard1, bonusNumImOfficial);
        }
    }

    // In updateSpecialNumberDisplay - missing null check for specialNumTextView1
    private void updateSpecialNumberDisplay(OfficialResults official) {
        String specialNumberText = null;

        // Prioritize the concatenated string from Special_Draws (e.g., for Lakshapathi)
        if (official.specialNumber != null && !official.specialNumber.isEmpty() && !official.specialNumber.equals("0")) {
            specialNumberText = official.specialNumber;
        }
        // Fallback to the single Long value from the "Special_Number" field
        else if (official.specialNum != null) {
            specialNumberText = String.valueOf(official.specialNum);
        }

        // Check if we found a valid special number to display
        if (specialNumberText != null) {
            specialNumTextView.setText(specialNumberText);
            // It seems you want to show the same official number in the user section
            if (specialNumTextView1 != null) {
                specialNumTextView1.setText(specialNumberText);
            }
            // This is the critical fix: make the views visible
            setViewsVisibility(View.VISIBLE, specialNumTextView, specialNumTextView1);
        } else {
            // Hide the views if no special number exists for this lottery
            setViewsVisibility(View.GONE, specialNumTextView, specialNumTextView1);
        }
    }

    private void compareNLBJayaNumbers(List<Long> official, List<Long> user) {
        if (official.size() != user.size() || official.isEmpty()) return;

        List<Long> reversedOfficial = new ArrayList<>(official);
        Collections.reverse(reversedOfficial);

        boolean forwardMatch = official.equals(user);
        boolean backwardMatch = reversedOfficial.equals(user);

        if (forwardMatch) matchResults.nlbJayaMatchType = "FORWARD";
        else if (backwardMatch) matchResults.nlbJayaMatchType = "BACKWARD";

        Set<Integer> matchedPositions = new HashSet<>();
        for (int i = 0; i < official.size(); i++) {
            if (official.get(i).equals(user.get(i))) {
                matchResults.nlbForwardMatchCount++;
                matchedPositions.add(i);
            }
            if (reversedOfficial.get(i).equals(user.get(i))) {
                matchResults.nlbBackwardMatchCount++;
                matchedPositions.add(i);
            }
        }
        matchResults.matchedNumbersCount = matchedPositions.size();
    }

    private void compareStandardNumbers(List<Long> official, List<Long> user) {
        Set<Long> userSet = new HashSet<>(user);
        Set<Long> officialSet = new HashSet<>(official);

        // Find common numbers between official and user
        for (Long officialNum : official) {
            if (userSet.contains(officialNum)) {
                matchResults.matchedNumbersList.add(officialNum);
            }
        }
        matchResults.matchedNumbersCount = matchResults.matchedNumbersList.size();

        // Store matched numbers set for easy lookup
        matchResults.matchedNumbersSet = new HashSet<>(matchResults.matchedNumbersList);
    }

    private void updateMatchIndicators() {
        setMatchImage(matchResults.isLetterMatched, englishLetterImOfficial, englishLetterImUser);
        setMatchImage(matchResults.isBonusMatched, bonusNumImOfficial, bonusNumImUser);

        String lotteryName = lotteryNameTextView.getText().toString();
        OfficialResults official = getCurrentOfficialResults();
        UserResults user = getCurrentUserResults();

        if (lotteryName.equals("NLB Jaya")) {
            updateNLBJayaMatchIndicators(official.numbersList, user.numbersList);
        } else {
            updateStandardMatchIndicators(official.numbersList, user.numbersList);
        }
    }

    private void updateStandardMatchIndicators(List<Long> official, List<Long> user) {
        // Check each official number
        for (int i = 0; i < officialNumImageViews.length && i < official.size(); i++) {
            boolean isMatched = user.contains(official.get(i));
            setMatchImage(isMatched, officialNumImageViews[i]);
        }

        // Check each user number
        for (int i = 0; i < userNumImageViews.length && i < user.size(); i++) {
            boolean isMatched = official.contains(user.get(i));
            setMatchImage(isMatched, userNumImageViews[i]);
        }
    }

    private void updateNLBJayaMatchIndicators(List<Long> official, List<Long> user) {
        if (official.size() != user.size() || official.isEmpty()) return;

        List<Long> reversedOfficial = new ArrayList<>(official);
        Collections.reverse(reversedOfficial);

        for (int i = 0; i < official.size() && i < 4; i++) {
            boolean forwardMatch = official.get(i).equals(user.get(i));
            boolean backwardMatch = reversedOfficial.get(i).equals(user.get(i));
            boolean finalMatch = forwardMatch || backwardMatch;

            if (i < officialNumImageViews.length) setMatchImage(finalMatch, officialNumImageViews[i]);
            if (i < userNumImageViews.length) setMatchImage(finalMatch, userNumImageViews[i]);
        }
    }

    private void setMatchImage(boolean isMatch, ImageView... imageViews) {
        int imageRes = isMatch ? R.drawable.done : R.drawable.close;
        for (ImageView imageView : imageViews) {
            if (imageView != null) {
                imageView.setImageResource(imageRes);
            }
        }
    }

    private void setViewsVisibility(int visibility, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(visibility);
            }
        }
    }

    // Updated showLoading method to use Lottie animation
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            contentLayout.setVisibility(View.GONE);
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation(); // Always start from the beginning
        } else {
            contentLayout.setVisibility(View.VISIBLE);
            loadingAnimation.setVisibility(View.GONE);
            loadingAnimation.cancelAnimation(); // Stop and reset the animation
        }
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showResultNotFound(long drawNumber) {
        Toast.makeText(this, "No results found for draw: " + drawNumber, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, ResultNotFoundActivity.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause animation when activity is paused to save resources
        if (loadingAnimation != null && loadingAnimation.isAnimating()) {
            loadingAnimation.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume animation if it was playing
        if (loadingAnimation != null && loadingAnimation.getVisibility() == View.VISIBLE) {
            loadingAnimation.resumeAnimation();
        }
    }

    // Helper Classes
    private static class LotteryConfig {
        final String collectionName;
        final int logoResource;

        LotteryConfig(String collectionName, int logoResource) {
            this.collectionName = collectionName;
            this.logoResource = logoResource;
        }
    }

    private static class MatchResults {
        int matchedNumbersCount = 0;
        boolean isLetterMatched = false;
        boolean isBonusMatched = false;
        ArrayList<Long> matchedNumbersList = new ArrayList<>();
        Set<Long> matchedNumbersSet = new HashSet<>(); // For easier lookup
        String nlbJayaMatchType = "NONE";
        int nlbForwardMatchCount = 0;
        int nlbBackwardMatchCount = 0;

        void reset() {
            matchedNumbersCount = 0;
            isLetterMatched = false;
            isBonusMatched = false;
            nlbJayaMatchType = "NONE";
            nlbForwardMatchCount = 0;
            nlbBackwardMatchCount = 0;
            matchedNumbersList.clear();
            matchedNumbersSet.clear();
        }

        void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra("LotteryName", lotteryName);
            intent.putExtra("IsLetterMatched", isLetterMatched);
            intent.putExtra("IsBonusMatched", isBonusMatched);
            intent.putExtra("MatchedNumbersCount", matchedNumbersCount);
            intent.putExtra("MatchedNumbersList", matchedNumbersList);
            intent.putExtra("NlbJayaMatchType", nlbJayaMatchType);
            intent.putExtra("NlbForwardMatchCount", nlbForwardMatchCount);
            intent.putExtra("NlbBackwardMatchCount", nlbBackwardMatchCount);
        }
    }

    // Instance variables to hold current results for match indicator updates
    private OfficialResults currentOfficialResults;
    private UserResults currentUserResults;

    private OfficialResults getCurrentOfficialResults() {
        return currentOfficialResults;
    }

    private UserResults getCurrentUserResults() {
        return currentUserResults;
    }

    private static class OfficialResults {
        public Long specialNum;
        Timestamp drawDate;
        Long drawNo;
        String engLetter;
        Long bonusNum; // This might be null for some lotteries
        List<Long> numbersList = new ArrayList<>();
        String specialNumber = ""; // To store the final string

        OfficialResults(DocumentSnapshot document) {
            drawDate = document.getTimestamp("Date");
            drawNo = document.getLong("Draw_No");
            engLetter = document.getString("English_Letter");
            bonusNum = document.getLong("Bonus_Number");
            specialNum = document.getLong("Special_Number");

            // Get the main numbers
            for (int i = 1; i <= 4; i++) {
                Long number = document.getLong("Number0" + i);
                if (number != null) {
                    numbersList.add(number);
                }
            }

            Long specialNumValue = document.getLong("Special_Number");
            if (specialNumValue != null) {
                specialNum = specialNumValue;
            }

            // Get the Special_Draws map
            Map<String, Object> specialDraws = (Map<String, Object>) document.get("Special_Draws");

            // Check if the map and the key exist
            if (specialDraws != null && specialDraws.containsKey("Lakshapathi_Double_Chance_No")) {

                // Get the list of numbers
                List<Long> specialNumberList = (List<Long>) specialDraws.get("Lakshapathi_Double_Chance_No");

                if (specialNumberList != null) {
                    // Use StringBuilder to efficiently build the string
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Long number : specialNumberList) {
                        stringBuilder.append(number.toString());
                    }
                    // Assign the final string to our class variable
                    this.specialNumber = stringBuilder.toString();
                }
            }
        }
    }

    private static class UserResults {
        String letter;
        Long bonusNum;
        List<Long> numbersList = new ArrayList<>();

        UserResults(List<String> winningNumbers, String singleLetter) {
            this.letter = singleLetter;

            boolean hasBonus = winningNumbers.size() >= 5;
            int startIndex = hasBonus ? 1 : 0;

            if (hasBonus) {
                try {
                    bonusNum = Long.parseLong(winningNumbers.get(0));
                } catch (NumberFormatException e) {
                    bonusNum = null;
                }
            }

            for (int i = 0; i < 4 && (startIndex + i) < winningNumbers.size(); i++) {
                try {
                    numbersList.add(Long.parseLong(winningNumbers.get(startIndex + i)));
                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
            }
        }
    }

    public class QRParser {
        private String lotteryName = "";
        private String drawNumber = "";
        private String singleLetter = "";
        private String secondLetter = ""; // New field for the second letter
        private String fiveDigitNumber = ""; // New field for the 5-digit number
        private List<String> winningNumbers = new ArrayList<>();
        private boolean valid = false;

        QRParser(String qrResult) {
            if (qrResult != null && !qrResult.isEmpty()) {
                parse(qrResult.trim());
            }
        }

        private void parse(String inputString) {
            String[] parts = inputString.split("\\s+");
            StringBuilder lotteryNameBuilder = new StringBuilder();

            // Define patterns for all parts you need to find
            Pattern drawNoPattern = Pattern.compile("\\d{4,}");
            Pattern letterPattern = Pattern.compile("[A-Z]");
            Pattern numberPattern = Pattern.compile("\\d{1,2}");
            Pattern fiveDigitPattern = Pattern.compile("\\d{5}"); // New pattern

            boolean drawNoFound = false;
            for (String part : parts) {
                // Find the draw number to separate the name from the data
                if (!drawNoFound && drawNoPattern.matcher(part).matches()) {
                    drawNumber = part;
                    drawNoFound = true;
                    continue; // Move to the next part of the string
                }

                if (!drawNoFound) {
                    // If draw number isn't found yet, it must be part of the lottery name
                    lotteryNameBuilder.append(part).append(" ");
                } else {
                    // After the draw number is found, parse the results data
                    if (singleLetter.isEmpty() && letterPattern.matcher(part).matches() && part.length() == 1) {
                        // Captures the FIRST letter (e.g., 'H')
                        singleLetter = part;
                    } else if (numberPattern.matcher(part).matches()) {
                        // Captures the main winning numbers (e.g., 35, 38, 41, 71)
                        winningNumbers.add(part);
                    } else if (!winningNumbers.isEmpty() && secondLetter.isEmpty() && letterPattern.matcher(part).matches() && part.length() == 1) {
                        // *** NEW: Captures the SECOND letter ('Z'), but only after the winning numbers have started
                        secondLetter = part;
                    } else if (fiveDigitPattern.matcher(part).matches()) {
                        // *** NEW: Captures the 5-digit number ('97969')
                        fiveDigitNumber = part;
                    }
                }
            }

            lotteryName = lotteryNameBuilder.toString().trim();
            // Update validation to ensure it's a valid QR
            valid = !lotteryName.isEmpty() && !drawNumber.isEmpty() && !winningNumbers.isEmpty();
        }

        // Getters for all the data
        boolean isValid() { return valid; }
        String getLotteryName() { return lotteryName; }
        String getDrawNumber() { return drawNumber; }
        String getSingleLetter() { return singleLetter; }
        List<String> getWinningNumbers() { return winningNumbers; }

        // New getters for your additional data
        public String getSecondLetter() { return secondLetter; }
        public String getFiveDigitNumber() { return fiveDigitNumber; }
    }
}