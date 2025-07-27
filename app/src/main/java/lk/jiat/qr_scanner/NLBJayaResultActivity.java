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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NLBJayaResultActivity extends BaseResultActivity {

    // UI Components
    private TextView lotteryNameTextView, dateTextView, drawNoTextView;
    private TextView officialLetter, userLetter;
    private final TextView[] officialNumTextViews = new TextView[4];
    private final TextView[] userNumTextViews = new TextView[4];
    private ImageView logoImageView;
    private final ImageView[] officialNumImageViews = new ImageView[4];
    private final ImageView[] userNumImageViews = new ImageView[4];
    private ImageView officialLetterIndicator, userLetterIndicator;

    private MatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_nlbjaya_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return "NLB_Jaya";
    }

    @Override
    protected void initializeViews() {
        matchResults = new MatchResults();

        logoImageView = findViewById(R.id.logo_image_view);
        lotteryNameTextView = findViewById(R.id.lottery_name);
        dateTextView = findViewById(R.id.date);
        drawNoTextView = findViewById(R.id.draw_no);

        // Official UI
        officialLetter = findViewById(R.id.english_letter);
        officialNumTextViews[0] = findViewById(R.id.num_01);
        officialNumTextViews[1] = findViewById(R.id.num_02);
        officialNumTextViews[2] = findViewById(R.id.num_03);
        officialNumTextViews[3] = findViewById(R.id.num_05);

        // User UI
        userLetter = findViewById(R.id.english_letter_user);
        userNumTextViews[0] = findViewById(R.id.num_05);
        userNumTextViews[1] = findViewById(R.id.num_06);
        userNumTextViews[2] = findViewById(R.id.num_07);
        userNumTextViews[3] = findViewById(R.id.num_08);

        // Indicators
        officialLetterIndicator = findViewById(R.id.english_letter_indicator2);
        userLetterIndicator = findViewById(R.id.english_letter_indicator);
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
                matchResults.addToIntent(intent, "NLB Jaya");
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
        compareNLBJayaResults(officialResults, userResults);
        updateNLBJayaMatchIndicators(officialResults, userResults);
    }

    private void updateOfficialDisplay(OfficialResults official) {
        logoImageView.setImageResource(R.drawable.nlb_jaya_logo); // drawable එකට nlb_jaya_logo.png දමන්න
        lotteryNameTextView.setText("NLB Jaya");
        if (official.drawDate != null) {
            dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(official.drawDate.toDate()));
        }
        drawNoTextView.setText(official.drawNo != null ? "Draw #" + official.drawNo : "N/A");
        officialLetter.setText(official.letter);

        for (int i = 0; i < officialNumTextViews.length; i++) {
            officialNumTextViews[i].setText(i < official.numbersList.size() ? String.valueOf(official.numbersList.get(i)) : "N/A");
        }
    }

    private void updateUserDisplay(UserResults user) {
        userLetter.setText(user.letter);
        for (int i = 0; i < userNumTextViews.length; i++) {
            userNumTextViews[i].setText(i < user.numbersList.size() ? String.valueOf(user.numbersList.get(i)) : "N/A");
        }
    }

    private void compareNLBJayaResults(OfficialResults official, UserResults user) {
        matchResults.isLetterMatched = official.letter != null && official.letter.equals(user.letter);

        List<Long> reversedOfficial = new ArrayList<>(official.numbersList);
        Collections.reverse(reversedOfficial);

        if (official.numbersList.equals(user.numbersList)) {
            matchResults.nlbJayaMatchType = "FORWARD";
        } else if (reversedOfficial.equals(user.numbersList)) {
            matchResults.nlbJayaMatchType = "BACKWARD";
        }

        for (int i = 0; i < official.numbersList.size(); i++) {
            if (i < user.numbersList.size() && official.numbersList.get(i).equals(user.numbersList.get(i))) {
                matchResults.nlbForwardMatchCount++;
            }
            if (i < user.numbersList.size() && reversedOfficial.get(i).equals(user.numbersList.get(i))) {
                matchResults.nlbBackwardMatchCount++;
            }
        }
    }

    private void updateNLBJayaMatchIndicators(OfficialResults official, UserResults user) {
        setMatchImage(matchResults.isLetterMatched, officialLetterIndicator, userLetterIndicator);

        List<Long> reversedOfficial = new ArrayList<>(official.numbersList);
        Collections.reverse(reversedOfficial);

        for (int i = 0; i < 4; i++) {
            boolean isMatched = false;
            if (i < official.numbersList.size() && i < user.numbersList.size()) {
                boolean forwardMatch = official.numbersList.get(i).equals(user.numbersList.get(i));
                boolean backwardMatch = reversedOfficial.get(i).equals(user.numbersList.get(i));
                isMatched = forwardMatch || backwardMatch;
            }
            // එකම Indicator එක Official සහ User යන දෙකටම set කරයි
            if (i < officialNumImageViews.length) setMatchImage(isMatched, officialNumImageViews[i], userNumImageViews[i]);
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
        boolean isLetterMatched = false;
        String nlbJayaMatchType = "NONE";
        int nlbForwardMatchCount = 0;
        int nlbBackwardMatchCount = 0;

        void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra("LotteryName", lotteryName);
            intent.putExtra("IsLetterMatched", isLetterMatched); // NLB Jaya වලදී IsLetter1Matched ලෙස යැවීම වඩා සුදුසුයි
            intent.putExtra("NlbJayaMatchType", nlbJayaMatchType);
            intent.putExtra("NlbForwardMatchCount", nlbForwardMatchCount);
            intent.putExtra("NlbBackwardMatchCount", nlbBackwardMatchCount);
        }
    }

    private static class OfficialResults {
        Timestamp drawDate;
        Long drawNo;
        String letter;
        final List<Long> numbersList = new ArrayList<>();

        OfficialResults(DocumentSnapshot doc) {
            this.drawDate = doc.getTimestamp("Date");
            this.drawNo = doc.getLong("Draw_No");
            this.letter = doc.getString("English_Letter");
            for (int i = 1; i <= 4; i++) {
                Long number = doc.getLong("Number0" + i);
                if (number != null) {
                    numbersList.add(number);
                }
            }
        }
    }

    private static class UserResults {
        String letter;
        final List<Long> numbersList = new ArrayList<>();

        UserResults(QRParser parser) {
            this.letter = parser.getSingleLetter();
            List<String> stringNumbers = parser.getWinningNumbers();
            for (String numStr : stringNumbers) {
                try {
                    numbersList.add(Long.parseLong(numStr));
                } catch (NumberFormatException e) { /* ignore */ }
            }
        }
    }
}