package lk.jiat.qr_scanner;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lk.jiat.qr_scanner.constants.LotteryConstants;

/**
 * Refactored DhanaNidhanayaResultActivity using the new base class and improved architecture.
 * Now focuses only on Dhana Nidhanaya specific logic.
 */
public class DhanaNidhanayaResultActivity extends BaseLotteryActivity {

    // Dhana Nidhanaya specific UI components
    private TextView officialLetter1, officialLetter2, official5Digit;
    private TextView userLetter1, userLetter2, user5Digit;
    private final TextView[] officialNumTextViews = new TextView[4];
    private final TextView[] userNumTextViews = new TextView[4];
    private final ImageView[] officialNumImageViews = new ImageView[4];
    private final ImageView[] userNumImageViews = new ImageView[4];
    private ImageView officialLetter1Indicator, officialLetter2Indicator, official5DigitIndicator;
    private ImageView userLetter1Indicator, userLetter2Indicator, user5DigitIndicator;

    // Match results specific to Dhana Nidhanaya
    private DhanaNidhanayaMatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dhana_nidhanaya_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return LotteryConstants.Collections.DHANA_NIDHANAYA;
    }

    @Override
    protected String getLotteryDisplayName() {
        return LotteryConstants.LotteryTypes.DHANA_NIDHANAYA;
    }

    @Override
    protected int getLogoResourceId() {
        return LotteryConstants.LotteryConfig.getLogoResource(LotteryConstants.LotteryTypes.DHANA_NIDHANAYA);
    }

    @Override
    protected void initializeSpecificViews() {
        matchResults = new DhanaNidhanayaMatchResults();

        // Initialize Dhana Nidhanaya specific UI components
        initializeLetterViews();
        initializeSpecialNumberViews();
        initializeNumberViews();
        initializeIndicatorViews();
    }

    private void initializeLetterViews() {
        // Official letters
        officialLetter1 = findViewById(R.id.english_letter);

        // User letters
        userLetter1 = findViewById(R.id.english_letter_user);
        userLetter2 = findViewById(R.id.second_english_letter_user);
    }

    private void initializeSpecialNumberViews() {
        // Special 5-digit numbers
        official5Digit = findViewById(R.id.special_number);
        user5Digit = findViewById(R.id.special_number_user);
    }

    private void initializeNumberViews() {
        // Official numbers
        officialNumTextViews[0] = findViewById(R.id.num_01);
        officialNumTextViews[1] = findViewById(R.id.num_02);
        officialNumTextViews[2] = findViewById(R.id.num_03);
        officialNumTextViews[3] = findViewById(R.id.num_05);

        // User numbers (corrected IDs to match the XML)
        userNumTextViews[0] = findViewById(R.id.num_05_user); // Now correctly named
        userNumTextViews[1] = findViewById(R.id.num_06);
        userNumTextViews[2] = findViewById(R.id.num_07);
        userNumTextViews[3] = findViewById(R.id.num_08);
    }

    private void initializeIndicatorViews() {
        // Official indicators
        officialLetter1Indicator = findViewById(R.id.english_letter_indicator2);
        official5DigitIndicator = findViewById(R.id.special_number_indicator);

        // User indicators
        userLetter1Indicator = findViewById(R.id.english_letter_indicator);
        userLetter2Indicator = findViewById(R.id.second_english_letter_indicator);
        user5DigitIndicator = findViewById(R.id.special_number_user_indicator);

        // Number indicators - Official
        officialNumImageViews[0] = findViewById(R.id.english_letter_indicator4);
        officialNumImageViews[1] = findViewById(R.id.english_letter_indicator5);
        officialNumImageViews[2] = findViewById(R.id.english_letter_indicator6);
        officialNumImageViews[3] = findViewById(R.id.english_letter_indicator7);

        // Number indicators - User
        userNumImageViews[0] = findViewById(R.id.num_05_indicator);
        userNumImageViews[1] = findViewById(R.id.num_06_indicator);
        userNumImageViews[2] = findViewById(R.id.num_07_indicator);
        userNumImageViews[3] = findViewById(R.id.num_08_indicator);
    }

    @Override
    protected void displayLotteryResults(DocumentSnapshot document, QRParser parser) {
        OfficialResults officialResults = new OfficialResults(document);
        UserResults userResults = new UserResults(parser);

        updateOfficialDisplay(officialResults);
        updateUserDisplay(userResults);
        compareResults(officialResults, userResults);
        updateMatchIndicators(officialResults, userResults);
    }

    @Override
    protected MatchResults getMatchResults() {
        return matchResults;
    }

    private void updateOfficialDisplay(OfficialResults official) {
        // Update letter
        if (officialLetter1 != null) {
            officialLetter1.setText(official.letter != null ? official.letter : "N/A");
        }

        // Update 5-digit special number
        if (official5Digit != null) {
            official5Digit.setText(official.fiveDigitNumber != null ? official.fiveDigitNumber : "N/A");
        }

        // Update regular numbers
        for (int i = 0; i < officialNumTextViews.length; i++) {
            if (officialNumTextViews[i] != null) {
                if (i < official.numbersList.size()) {
                    officialNumTextViews[i].setText(String.valueOf(official.numbersList.get(i)));
                } else {
                    officialNumTextViews[i].setText("N/A");
                }
            }
        }
    }

    private void updateUserDisplay(UserResults user) {
        // Update letters
        if (userLetter1 != null) {
            userLetter1.setText(user.letter1 != null ? user.letter1 : "N/A");
        }
        if (userLetter2 != null) {
            userLetter2.setText(user.letter2 != null ? user.letter2 : "N/A");
        }

        // Update 5-digit special number
        if (user5Digit != null) {
            user5Digit.setText(user.fiveDigitNumber != null ? user.fiveDigitNumber : "N/A");
        }

        // Update regular numbers
        for (int i = 0; i < userNumTextViews.length; i++) {
            if (userNumTextViews[i] != null) {
                if (i < user.numbersList.size()) {
                    userNumTextViews[i].setText(user.numbersList.get(i));
                } else {
                    userNumTextViews[i].setText("N/A");
                }
            }
        }
    }

    private void compareResults(OfficialResults official, UserResults user) {
        // Compare letters - User's two letters against official's single letter
        String officialLetter = official.letter;
        if (officialLetter != null) {
            matchResults.isLetter1Matched = officialLetter.equals(user.letter1);
            matchResults.isLetter2Matched = officialLetter.equals(user.letter2);
        }

        // Compare 5-digit special number
        matchResults.is5DigitMatched = official.fiveDigitNumber != null &&
                official.fiveDigitNumber.equals(user.fiveDigitNumber);

        // Compare regular numbers
        Set<Long> officialNumbersSet = new HashSet<>();
        for (String numStr : official.numbersList) {
            try {
                officialNumbersSet.add(Long.parseLong(numStr));
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }

        for (String userNumStr : user.numbersList) {
            try {
                long userNum = Long.parseLong(userNumStr);
                if (officialNumbersSet.contains(userNum)) {
                    matchResults.matchedNumbersCount++;
                    matchResults.matchedNumbersList.add(userNum);
                }
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
    }

    private void updateMatchIndicators(OfficialResults official, UserResults user) {
        // Set letter match indicators
        setMatchImage(matchResults.isLetter1Matched, officialLetter1Indicator, userLetter1Indicator);
        setMatchImage(matchResults.isLetter2Matched, userLetter2Indicator);
        setMatchImage(matchResults.is5DigitMatched, official5DigitIndicator, user5DigitIndicator);

        // Set number match indicators
        Set<String> userNumbersSet = new HashSet<>(user.numbersList);
        Set<String> officialNumbersSet = new HashSet<>(official.numbersList);

        // Official numbers indicators
        for (int i = 0; i < officialNumImageViews.length && i < official.numbersList.size(); i++) {
            if (officialNumImageViews[i] != null) {
                boolean isMatched = userNumbersSet.contains(official.numbersList.get(i));
                setMatchImage(isMatched, officialNumImageViews[i]);
            }
        }

        // User numbers indicators
        for (int i = 0; i < userNumImageViews.length && i < user.numbersList.size(); i++) {
            if (userNumImageViews[i] != null) {
                boolean isMatched = officialNumbersSet.contains(user.numbersList.get(i));
                setMatchImage(isMatched, userNumImageViews[i]);
            }
        }
    }

    // --- Helper Classes for Dhana Nidhanaya ---

    private static class DhanaNidhanayaMatchResults extends MatchResults {
        boolean isLetter1Matched = false;
        boolean isLetter2Matched = false;
        boolean is5DigitMatched = false;
        int matchedNumbersCount = 0;
        ArrayList<Long> matchedNumbersList = new ArrayList<>();

        @Override
        public void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra(LotteryConstants.IntentExtras.LOTTERY_NAME, lotteryName);
            intent.putExtra(LotteryConstants.IntentExtras.IS_LETTER_MATCHED, isLetter1Matched);
            intent.putExtra(LotteryConstants.IntentExtras.IS_LETTER2_MATCHED, isLetter2Matched);
            intent.putExtra(LotteryConstants.IntentExtras.IS_5DIGIT_MATCHED, is5DigitMatched);
            intent.putExtra(LotteryConstants.IntentExtras.MATCHED_NUMBERS_COUNT, matchedNumbersCount);
            intent.putExtra(LotteryConstants.IntentExtras.MATCHED_NUMBERS_LIST, matchedNumbersList);
        }
    }

    private static class OfficialResults {
        String letter;
        String fiveDigitNumber = "";
        final List<String> numbersList = new ArrayList<>();

        OfficialResults(DocumentSnapshot doc) {
            this.letter = doc.getString(LotteryConstants.FirestoreFields.ENGLISH_LETTER);

            // Get winning numbers (Number01-04)
            for (int i = 1; i <= 4; i++) {
                Long number = doc.getLong(LotteryConstants.FirestoreFields.NUMBER_01.replace("01", String.format("%02d", i)));
                if (number != null) {
                    numbersList.add(String.valueOf(number));
                }
            }

            // Get special 5-digit number from Special_Draws
            try {
                Object specialDrawsObj = doc.get(LotteryConstants.FirestoreFields.SPECIAL_DRAWS);
                if (specialDrawsObj instanceof Map) {
                    Map<String, Object> specialDraws = (Map<String, Object>) specialDrawsObj;
                    Object listObj = specialDraws.get(LotteryConstants.FirestoreFields.LAKSHAPATHI_DOUBLE_CHANCE_NO);
                    if (listObj instanceof List) {
                        List<?> specialNumberList = (List<?>) listObj;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Object item : specialNumberList) {
                            if (item != null) {
                                stringBuilder.append(item.toString());
                            }
                        }
                        this.fiveDigitNumber = stringBuilder.toString();
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("DhanaNidhanaya", "Error parsing special number", e);
                this.fiveDigitNumber = "";
            }
        }
    }

    private static class UserResults {
        String letter1;
        String letter2;
        String fiveDigitNumber;
        final List<String> numbersList;

        UserResults(QRParser parser) {
            this.letter1 = parser.getSingleLetter();
            this.letter2 = parser.getSecondLetter();
            this.fiveDigitNumber = parser.getFiveDigitNumber();
            this.numbersList = parser.getWinningNumbers();
        }
    }
}