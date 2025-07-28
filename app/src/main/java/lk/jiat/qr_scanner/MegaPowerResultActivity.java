package lk.jiat.qr_scanner;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import lk.jiat.qr_scanner.constants.LotteryConstants;

/**
 * MegaPowerResultActivity with Color-Based Match System
 * Indicators නැතුව, colors වෙනස් කරන version එක
 */
public class MegaPowerResultActivity extends BaseLotteryActivity {

    // UI Components
    private TextView englishLetterTextView, bonusNumTextView;
    private TextView englishLetterTextViewUser, bonusNumTextViewUser;
    private final TextView[] officialNumTextViews = new TextView[4];
    private final TextView[] userNumTextViews = new TextView[4];

    // CardView references for color changes
    private final CardView[] officialCards = new CardView[6];  // Letter, Bonus, 4 Numbers
    private final CardView[] userCards = new CardView[6];

    // ImageView references (to hide them)
    private final ImageView[] officialIndicators = new ImageView[6];
    private final ImageView[] userIndicators = new ImageView[6];

    private CardView bonusNumberCard, bonusNumberCardUser;

    // Match results
    private MegaPowerMatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_mega_power_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return LotteryConstants.Collections.MEGA_POWER;
    }

    @Override
    protected String getLotteryDisplayName() {
        return LotteryConstants.LotteryTypes.MEGA_POWER;
    }

    @Override
    protected int getLogoResourceId() {
        return LotteryConstants.LotteryConfig.getLogoResource(LotteryConstants.LotteryTypes.MEGA_POWER);
    }

    @Override
    protected void initializeSpecificViews() {
        matchResults = new MegaPowerMatchResults();

        // Initialize text views
        englishLetterTextView = findViewById(R.id.english_letter);
        bonusNumTextView = findViewById(R.id.bonusNumber);
        englishLetterTextViewUser = findViewById(R.id.english_letter_user);
        bonusNumTextViewUser = findViewById(R.id.bonus_num_user);

        // Initialize number text views
        officialNumTextViews[0] = findViewById(R.id.num_01);
        officialNumTextViews[1] = findViewById(R.id.num_02);
        officialNumTextViews[2] = findViewById(R.id.num_03);
        officialNumTextViews[3] = findViewById(R.id.num_04);

        userNumTextViews[0] = findViewById(R.id.num_05);
        userNumTextViews[1] = findViewById(R.id.num_06);
        userNumTextViews[2] = findViewById(R.id.num_07);
        userNumTextViews[3] = findViewById(R.id.num_08);

        // Initialize CardViews for color changes
        initializeCardViews();

        // Initialize indicators (to hide them)
        initializeIndicators();

        // Initialize bonus card views
        bonusNumberCard = findViewById(R.id.bonusNumberCard);
        bonusNumberCardUser = findViewById(R.id.bonusNumberCard1);
    }

    private void initializeCardViews() {
        // Find CardViews by finding parent of TextViews
        if (englishLetterTextView != null) {
            officialCards[0] = (CardView) englishLetterTextView.getParent();
        }
        if (bonusNumTextView != null) {
            officialCards[1] = (CardView) bonusNumTextView.getParent();
        }

        // Official number cards
        for (int i = 0; i < officialNumTextViews.length; i++) {
            if (officialNumTextViews[i] != null) {
                officialCards[i + 2] = (CardView) officialNumTextViews[i].getParent();
            }
        }

        // User cards
        if (englishLetterTextViewUser != null) {
            userCards[0] = (CardView) englishLetterTextViewUser.getParent();
        }
        if (bonusNumTextViewUser != null) {
            userCards[1] = (CardView) bonusNumTextViewUser.getParent();
        }

        // User number cards
        for (int i = 0; i < userNumTextViews.length; i++) {
            if (userNumTextViews[i] != null) {
                userCards[i + 2] = (CardView) userNumTextViews[i].getParent();
            }
        }
    }

    private void initializeIndicators() {
        // Official indicators
        officialIndicators[0] = findViewById(R.id.english_letter_indicator2);
        officialIndicators[1] = findViewById(R.id.english_letter_indicator3);
        officialIndicators[2] = findViewById(R.id.english_letter_indicator4);
        officialIndicators[3] = findViewById(R.id.english_letter_indicator5);
        officialIndicators[4] = findViewById(R.id.english_letter_indicator6);
        officialIndicators[5] = findViewById(R.id.english_letter_indicator7);

        // User indicators
        userIndicators[0] = findViewById(R.id.english_letter_indicator);
        userIndicators[1] = findViewById(R.id.bonus_num_indicator);
        userIndicators[2] = findViewById(R.id.num_05_indicator);
        userIndicators[3] = findViewById(R.id.num_06_indicator);
        userIndicators[4] = findViewById(R.id.num_07_indicator);
        userIndicators[5] = findViewById(R.id.num_08_indicator);
    }

    @Override
    protected void displayLotteryResults(DocumentSnapshot document, QRParser parser) {
        OfficialResults officialResults = new OfficialResults(document);
        UserResults userResults = new UserResults(parser.getWinningNumbers(), parser.getSingleLetter());

        updateOfficialDisplay(officialResults);
        updateUserDisplay(userResults);
        updateMatchColorsImproved(officialResults, userResults);
    }

    @Override
    protected MatchResults getMatchResults() {
        return matchResults;
    }

    private void updateOfficialDisplay(OfficialResults official) {
        englishLetterTextView.setText(official.engLetter != null ? official.engLetter : "N/A");

        // Bonus number visibility
        if (official.bonusNum != null) {
            bonusNumTextView.setText(String.valueOf(official.bonusNum));
            bonusNumberCard.setVisibility(View.VISIBLE);
        } else {
            bonusNumberCard.setVisibility(View.GONE);
        }

        // Regular numbers
        for (int i = 0; i < officialNumTextViews.length; i++) {
            if (i < official.numbersList.size()) {
                officialNumTextViews[i].setText(String.valueOf(official.numbersList.get(i)));
            } else {
                officialNumTextViews[i].setText("N/A");
            }
        }
    }

    private void updateUserDisplay(UserResults user) {
        englishLetterTextViewUser.setText(user.letter != null ? user.letter : "N/A");

        // Bonus number visibility
        if (user.bonusNum != null) {
            bonusNumTextViewUser.setText(String.valueOf(user.bonusNum));
            bonusNumberCardUser.setVisibility(View.VISIBLE);
        } else {
            bonusNumberCardUser.setVisibility(View.GONE);
        }

        // Regular numbers
        for (int i = 0; i < userNumTextViews.length; i++) {
            if (i < user.numbersList.size()) {
                userNumTextViews[i].setText(String.valueOf(user.numbersList.get(i)));
            } else {
                userNumTextViews[i].setText("N/A");
            }
        }
    }

    /**
     * Update match colors using the new color-based system
     */
    private void updateMatchColorsImproved(OfficialResults official, UserResults user) {
        // Prepare data arrays
        List<String> officialData = new ArrayList<>();
        List<String> userData = new ArrayList<>();

        // Add letter
        officialData.add(official.engLetter != null ? official.engLetter : "");
        userData.add(user.letter != null ? user.letter : "");

        // Add bonus (if exists)
        if (official.bonusNum != null && user.bonusNum != null) {
            officialData.add(String.valueOf(official.bonusNum));
            userData.add(String.valueOf(user.bonusNum));
        } else {
            officialData.add("");
            userData.add("");
        }

        // Add numbers
        for (Long num : official.numbersList) {
            officialData.add(String.valueOf(num));
        }
        for (Long num : user.numbersList) {
            userData.add(String.valueOf(num));
        }

        // Convert to arrays
        String[] officialArray = officialData.toArray(new String[0]);
        String[] userArray = userData.toArray(new String[0]);

        // Use color-based system
        ColorBasedMatchSystem.ColorMatchResult result = ColorBasedMatchSystem.updateMegaPowerWithColors(
                officialArray,
                userArray,
                officialCards,
                userCards,
                officialIndicators,
                userIndicators,
                this
        );

        // Update match results for prize calculation
        matchResults.isLetterMatched = result.isLetterMatched;
        matchResults.isBonusMatched = result.isBonusMatched;
        matchResults.matchedNumbersCount = result.matchedNumbersCount;

        // Convert matched numbers to Long list
        matchResults.matchedNumbersList.clear();
        for (String numStr : result.matchedNumbers) {
            try {
                matchResults.matchedNumbersList.add(Long.parseLong(numStr));
            } catch (NumberFormatException e) {
                android.util.Log.w("MegaPower", "Invalid number: " + numStr);
            }
        }

        // Debug logging
        ColorBasedMatchSystem.logColorMatchResults(result, "Mega Power");

        android.util.Log.d("MegaPower", "=== Color-Based Results ===");
        android.util.Log.d("MegaPower", "Letter: " + official.engLetter + " vs " + user.letter + " = " + result.isLetterMatched);
        android.util.Log.d("MegaPower", "Bonus: " + official.bonusNum + " vs " + user.bonusNum + " = " + result.isBonusMatched);
        android.util.Log.d("MegaPower", "Numbers: " + result.matchedNumbersCount + " matches");
    }

    // Helper Classes (same as before)
    private static class MegaPowerMatchResults extends MatchResults {
        boolean isLetterMatched = false;
        boolean isBonusMatched = false;
        int matchedNumbersCount = 0;
        ArrayList<Long> matchedNumbersList = new ArrayList<>();

        @Override
        public void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra(LotteryConstants.IntentExtras.LOTTERY_NAME, lotteryName);
            intent.putExtra(LotteryConstants.IntentExtras.IS_LETTER_MATCHED, isLetterMatched);
            intent.putExtra(LotteryConstants.IntentExtras.IS_BONUS_MATCHED, isBonusMatched);
            intent.putExtra(LotteryConstants.IntentExtras.MATCHED_NUMBERS_COUNT, matchedNumbersCount);
            intent.putExtra(LotteryConstants.IntentExtras.MATCHED_NUMBERS_LIST, matchedNumbersList);
        }
    }

    private static class OfficialResults {
        String engLetter;
        Long bonusNum;
        final List<Long> numbersList = new ArrayList<>();

        OfficialResults(DocumentSnapshot doc) {
            this.engLetter = doc.getString(LotteryConstants.FirestoreFields.ENGLISH_LETTER);
            this.bonusNum = doc.getLong(LotteryConstants.FirestoreFields.BONUS_NUMBER);

            for (int i = 1; i <= 4; i++) {
                String fieldName = "Number0" + i;
                Long number = doc.getLong(fieldName);
                if (number != null) {
                    numbersList.add(number);
                }
            }
        }
    }

    private static class UserResults {
        String letter;
        Long bonusNum;
        final List<Long> numbersList = new ArrayList<>();

        UserResults(List<String> qrNumbers, String qrLetter) {
            this.letter = qrLetter;

            boolean hasBonus = qrNumbers.size() >= 5;
            int startIndex = hasBonus ? 1 : 0;

            if (hasBonus) {
                try {
                    this.bonusNum = Long.parseLong(qrNumbers.get(0));
                } catch (NumberFormatException e) {
                    this.bonusNum = null;
                }
            }

            for (int i = 0; i < 4 && (startIndex + i) < qrNumbers.size(); i++) {
                try {
                    numbersList.add(Long.parseLong(qrNumbers.get(startIndex + i)));
                } catch (NumberFormatException e) {
                    android.util.Log.w("MegaPower", "Invalid user number: " + qrNumbers.get(startIndex + i));
                }
            }
        }
    }
}