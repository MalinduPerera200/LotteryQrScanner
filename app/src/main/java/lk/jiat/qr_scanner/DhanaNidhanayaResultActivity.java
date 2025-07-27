package lk.jiat.qr_scanner;

import android.content.Intent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DhanaNidhanayaResultActivity extends BaseResultActivity {

    // UI Components
    private TextView lotteryNameTextView, dateTextView, drawNoTextView;
    private TextView officialLetter1, officialLetter2, official5Digit;
    private TextView userLetter1, userLetter2, user5Digit;
    private final TextView[] officialNumTextViews = new TextView[4];
    private final TextView[] userNumTextViews = new TextView[4];
    private ImageView logoImageView;
    private final ImageView[] officialNumImageViews = new ImageView[4];
    private final ImageView[] userNumImageViews = new ImageView[4];
    private ImageView officialLetter1Indicator, officialLetter2Indicator, official5DigitIndicator;
    private ImageView userLetter1Indicator, userLetter2Indicator, user5DigitIndicator;

    private MatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dhana_nidhanaya_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return "Dhana_Nidhanaya";
    }

    @Override
    protected void initializeViews() {
        matchResults = new MatchResults();

        logoImageView = findViewById(R.id.logo_image_view);
        lotteryNameTextView = findViewById(R.id.lottery_name);
        dateTextView = findViewById(R.id.date);
        drawNoTextView = findViewById(R.id.draw_no);

        // Official UI
        officialLetter1 = findViewById(R.id.english_letter);
        official5Digit = findViewById(R.id.special_number);
        officialNumTextViews[0] = findViewById(R.id.num_01);
        officialNumTextViews[1] = findViewById(R.id.num_02);
        officialNumTextViews[2] = findViewById(R.id.num_03);
        officialNumTextViews[3] = findViewById(R.id.num_05);

        // User UI
        userLetter1 = findViewById(R.id.english_letter_user);
        userLetter2 = findViewById(R.id.second_english_letter_user);
        user5Digit = findViewById(R.id.special_number_user); // <<< නිවැරදි කරන ලදී
        userNumTextViews[0] = findViewById(R.id.num_05);
        userNumTextViews[1] = findViewById(R.id.num_06);
        userNumTextViews[2] = findViewById(R.id.num_07);
        userNumTextViews[3] = findViewById(R.id.num_08);

        // Indicators
        officialLetter1Indicator = findViewById(R.id.english_letter_indicator2);
        official5DigitIndicator = findViewById(R.id.special_number_indicator);
        userLetter1Indicator = findViewById(R.id.english_letter_indicator);
        userLetter2Indicator = findViewById(R.id.second_english_letter_indicator);
        user5DigitIndicator = findViewById(R.id.special_number_user_indicator);
        officialNumImageViews[0] = findViewById(R.id.english_letter_indicator4);
        officialNumImageViews[1] = findViewById(R.id.english_letter_indicator5);
        officialNumImageViews[2] = findViewById(R.id.english_letter_indicator6);
        officialNumImageViews[3] = findViewById(R.id.english_letter_indicator7);
        userNumImageViews[0] = findViewById(R.id.num_05_indicator);
        userNumImageViews[1] = findViewById(R.id.num_06_indicator);
        userNumImageViews[2] = findViewById(R.id.num_07_indicator);
        userNumImageViews[3] = findViewById(R.id.num_08_indicator);

        if (viewPriceButton != null) {
            viewPriceButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click_animation));
                Intent intent = new Intent(this, ViewPriceActivity.class);
                matchResults.addToIntent(intent, "Dhana Nidhanaya");
                startActivity(intent);
            });
        }
    }

    @Override
    protected void displayAndCompareResults(DocumentSnapshot document, QRParser parser) {
        OfficialResults officialResults = new OfficialResults(document);
        UserResults userResults = new UserResults(parser);

        updateOfficialDisplay(officialResults);
        updateUserDisplay(userResults);
        compareResults(officialResults, userResults);
        updateMatchIndicators(officialResults, userResults);
    }

    private void updateOfficialDisplay(OfficialResults official) {
        logoImageView.setImageResource(R.drawable.dana_nidanaya);
        lotteryNameTextView.setText("Dhana Nidhanaya");
        if (official.drawDate != null) {
            dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(official.drawDate.toDate()));
        }
        drawNoTextView.setText(official.drawNo != null ? "Draw #" + official.drawNo : "N/A");

        officialLetter1.setText(official.letter);
        official5Digit.setText(official.fiveDigitNumber);


        for (int i = 0; i < officialNumTextViews.length; i++) {
            if (i < official.numbersList.size()) {
                officialNumTextViews[i].setText(String.valueOf(official.numbersList.get(i)));
            } else {
                officialNumTextViews[i].setText("N/A");
            }
        }
    }

    private void updateUserDisplay(UserResults user) {
        userLetter1.setText(user.letter1);
        userLetter2.setText(user.letter2);
        user5Digit.setText(user.fiveDigitNumber);

        for (int i = 0; i < userNumTextViews.length; i++) {
            if (i < user.numbersList.size()) {
                userNumTextViews[i].setText(user.numbersList.get(i));
            } else {
                userNumTextViews[i].setText("N/A");
            }
        }
    }

    private void compareResults(OfficialResults official, UserResults user) {
        // <<< CHANGE: User ගේ අකුරු දෙකම, Official අකුරට සංසන්දනය කිරීම >>>
        String officialLetter = official.letter;

        if (officialLetter != null) {
            matchResults.isLetter1Matched = officialLetter.equals(user.letter1);
            matchResults.isLetter2Matched = officialLetter.equals(user.letter2);
        }

        matchResults.is5DigitMatched = official.fiveDigitNumber != null && official.fiveDigitNumber.equals(user.fiveDigitNumber);

        Set<Long> officialNumbersSet = new HashSet<>();
        for (String numStr : official.numbersList) {
            try {
                officialNumbersSet.add(Long.parseLong(numStr));
            } catch (NumberFormatException e) { /* ignore */ }
        }

        for (String userNumStr : user.numbersList) {
            try {
                if (officialNumbersSet.contains(Long.parseLong(userNumStr))) {
                    matchResults.matchedNumbersCount++;
                    matchResults.matchedNumbersList.add(Long.parseLong(userNumStr));
                }
            } catch (NumberFormatException e) { /* ignore */ }
        }
    }

    private void updateMatchIndicators(OfficialResults official, UserResults user) {
        setMatchImage(matchResults.isLetter1Matched, officialLetter1Indicator, userLetter1Indicator);
        setMatchImage(matchResults.isLetter2Matched, officialLetter2Indicator, userLetter2Indicator);
        setMatchImage(matchResults.is5DigitMatched, official5DigitIndicator, user5DigitIndicator);

        Set<String> userNumbersSet = new HashSet<>(user.numbersList);
        Set<String> officialNumbersSet = new HashSet<>(official.numbersList);

        for (int i = 0; i < officialNumImageViews.length && i < official.numbersList.size(); i++) {
            setMatchImage(userNumbersSet.contains(official.numbersList.get(i)), officialNumImageViews[i]);
        }

        for (int i = 0; i < userNumImageViews.length && i < user.numbersList.size(); i++) {
            setMatchImage(officialNumbersSet.contains(user.numbersList.get(i)), userNumImageViews[i]);
        }
    }

    private void setMatchImage(boolean isMatch, ImageView... imageViews) {
        int imageRes = isMatch ? R.drawable.done : R.drawable.close;
        for (ImageView imageView : imageViews) {
            if (imageView != null) {
                imageView.setImageResource(imageRes);
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    // --- Helper Classes ---

    private static class MatchResults {
        boolean isLetter1Matched, isLetter2Matched, is5DigitMatched = false;
        int matchedNumbersCount = 0;
        ArrayList<Long> matchedNumbersList = new ArrayList<>();

        void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra("LotteryName", lotteryName);
            intent.putExtra("IsLetter1Matched", isLetter1Matched);
            intent.putExtra("IsLetter2Matched", isLetter2Matched);
            intent.putExtra("Is5DigitMatched", is5DigitMatched);
            intent.putExtra("MatchedNumbersCount", matchedNumbersCount);
            intent.putExtra("MatchedNumbersList", matchedNumbersList);
        }
    }

    private static class OfficialResults {
        Timestamp drawDate;
        Long drawNo;
        String letter;
        String fiveDigitNumber = ""; // Initialize as empty
        final List<String> numbersList = new ArrayList<>();

        OfficialResults(DocumentSnapshot doc) {
            this.drawDate = doc.getTimestamp("Date"); //
            this.drawNo = doc.getLong("Draw_No"); //
            this.letter = doc.getString("English_Letter"); //

            // --- Winning Numbers (Number01-04) ලබා ගැනීම ---
            for (int i = 1; i <= 4; i++) {
                // දත්ත ගබඩාවේ අංක 'Long' ලෙස ඇති නිසා, සෘජුවම getLong() භාවිතා කරයි
                Long number = doc.getLong("Number0" + i); //
                if (number != null) {
                    numbersList.add(String.valueOf(number));
                }
            }

            // --- Special Number (Lakshapathi_Double_Chance_No) ලබා ගැනීම ---
            try {
                // 1. 'Special_Draws' නමැති Map එක ලබා ගනී
                Object specialDrawsObj = doc.get("Special_Draws"); //
                if (specialDrawsObj instanceof Map) {
                    Map<String, Object> specialDraws = (Map<String, Object>) specialDrawsObj;

                    // 2. එම Map එකෙන් 'Lakshapathi_Double_Chance_No' නමැති List එක ලබා ගනී
                    Object listObj = specialDraws.get("Lakshapathi_Double_Chance_No"); //
                    if (listObj instanceof List) {
                        List<?> specialNumberList = (List<?>) listObj;

                        // 3. List එකේ ඇති අංක එකතු කර තනි String එකක් සාදයි
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Object item : specialNumberList) {
                            if (item != null) {
                                stringBuilder.append(item.toString());
                            }
                        }
                        this.fiveDigitNumber = stringBuilder.toString(); // "54760"
                    }
                }
            } catch (Exception e) {
                // දෝෂයක් ඇති වුවහොත්, fiveDigitNumber එක හිස්ව පවතී
                this.fiveDigitNumber = "";
                // Log the error if needed
            }
        }
    }

    private static class UserResults {
        String letter1, letter2, fiveDigitNumber;
        final List<String> numbersList;

        UserResults(QRParser parser) {
            this.letter1 = parser.getSingleLetter();
            this.letter2 = parser.getSecondLetter();
            this.fiveDigitNumber = parser.getFiveDigitNumber();
            this.numbersList = parser.getWinningNumbers();
        }
    }
}