package lk.jiat.qr_scanner;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Set;

public class GoviSethaResultActivity extends BaseResultActivity {

    // UI Components
    private TextView lotteryNameTextView, dateTextView, drawNoTextView;
    private TextView officialLetter1, officialLetter2;
    private TextView userLetter1, userLetter2;
    private final TextView[] officialNumTextViews = new TextView[4];
    private final TextView[] userNumTextViews = new TextView[4];
    private ImageView logoImageView;
    private final ImageView[] officialNumImageViews = new ImageView[4];
    private final ImageView[] userNumImageViews = new ImageView[4];
    private ImageView officialLetter1Indicator, officialLetter2Indicator;
    private ImageView userLetter1Indicator, userLetter2Indicator;

    private MatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_govi_setha_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return "Govi_Setha";
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
        officialNumTextViews[0] = findViewById(R.id.num_01);
        officialNumTextViews[1] = findViewById(R.id.num_02);
        officialNumTextViews[2] = findViewById(R.id.num_03);
        officialNumTextViews[3] = findViewById(R.id.num_04);

        // User UI
        userLetter1 = findViewById(R.id.english_letter_user);
        userLetter2 = findViewById(R.id.second_english_letter_user);
        userNumTextViews[0] = findViewById(R.id.num_05);
        userNumTextViews[1] = findViewById(R.id.num_06);
        userNumTextViews[2] = findViewById(R.id.num_07);
        userNumTextViews[3] = findViewById(R.id.num_08);

        // Indicators
        officialLetter1Indicator = findViewById(R.id.english_letter_indicator2);
        userLetter1Indicator = findViewById(R.id.english_letter_indicator);
        userLetter2Indicator = findViewById(R.id.second_english_letter_indicator);
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
        logoImageView.setImageResource(R.drawable.govisetha); // drawable එකට govisetha.png දමන්න
        lotteryNameTextView.setText("Govi Setha");
        if (official.drawDate != null) {
            dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(official.drawDate.toDate()));
        }
        drawNoTextView.setText(official.drawNo != null ? "Draw #" + official.drawNo : "N/A");

        officialLetter1.setText(official.letter1);
        officialLetter2.setText(official.letter2);

        for (int i = 0; i < officialNumTextViews.length; i++) {
            officialNumTextViews[i].setText(i < official.numbersList.size() ? String.valueOf(official.numbersList.get(i)) : "N/A");
        }
    }

    private void updateUserDisplay(UserResults user) {
        userLetter1.setText(user.letter1);
        userLetter2.setText(user.letter2);
        for (int i = 0; i < userNumTextViews.length; i++) {
            userNumTextViews[i].setText(i < user.numbersList.size() ? String.valueOf(user.numbersList.get(i)) : "N/A");
        }
    }

    private void compareResults(OfficialResults official, UserResults user) {
        matchResults.isLetter1Matched = official.letter1 != null && official.letter1.equals(user.letter1);
        matchResults.isLetter2Matched = official.letter2 != null && official.letter2.equals(user.letter2);

        Set<Long> officialNumbersSet = new HashSet<>(official.numbersList);
        for (Long userNum : user.numbersList) {
            if (officialNumbersSet.contains(userNum)) {
                matchResults.matchedNumbersCount++;
            }
        }
    }

    private void updateMatchIndicators(OfficialResults official, UserResults user) {
        setMatchImage(matchResults.isLetter1Matched, officialLetter1Indicator, userLetter1Indicator);
        setMatchImage(matchResults.isLetter2Matched, officialLetter2Indicator, userLetter2Indicator);

        Set<Long> officialNumbersSet = new HashSet<>(official.numbersList);
        Set<Long> userNumbersSet = new HashSet<>(user.numbersList);

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
        boolean isLetter1Matched = false;
        boolean isLetter2Matched = false;
        int matchedNumbersCount = 0;

        void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra("LotteryName", lotteryName);
            intent.putExtra("IsLetter1Matched", isLetter1Matched);
            intent.putExtra("IsLetter2Matched", isLetter2Matched);
            intent.putExtra("MatchedNumbersCount", matchedNumbersCount);
        }
    }

    private static class OfficialResults {
        Timestamp drawDate;
        Long drawNo;
        String letter1, letter2;
        final List<Long> numbersList = new ArrayList<>();

        OfficialResults(DocumentSnapshot doc) {
            this.drawDate = doc.getTimestamp("Date");
            this.drawNo = doc.getLong("Draw_No");
            this.letter1 = doc.getString("English_Letter");
            this.letter2 = doc.getString("English_Letter_2"); // Firestore එකේ දෙවන අකුරේ field name එක

            for (int i = 1; i <= 4; i++) {
                Long number = doc.getLong("Number0" + i);
                if (number != null) {
                    numbersList.add(number);
                }
            }
        }
    }

    private static class UserResults {
        String letter1, letter2;
        final List<Long> numbersList = new ArrayList<>();

        UserResults(QRParser parser) {
            this.letter1 = parser.getSingleLetter();
            this.letter2 = parser.getSecondLetter();
            List<String> stringNumbers = parser.getWinningNumbers();

            for (String numStr : stringNumbers) {
                try {
                    numbersList.add(Long.parseLong(numStr));
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers
                }
            }
        }
    }
}