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
import java.util.Locale;

public class MahajanaSampathaResultActivity extends BaseResultActivity {

    // UI Components
    private TextView lotteryNameTextView, dateTextView, drawNoTextView;
    private TextView officialLetter, userLetter;
    private ImageView logoImageView, officialLetterIndicator, userLetterIndicator;

    private final TextView[] officialNumTvs = new TextView[6];
    private final TextView[] userNumTvs = new TextView[6];
    private final ImageView[] officialNumIndicators = new ImageView[6];
    private final ImageView[] userNumIndicators = new ImageView[6];

    private MatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_mahajana_sampatha_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return "Mahajana_Sampatha";
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
        officialNumTvs[0] = findViewById(R.id.num_01);
        officialNumTvs[1] = findViewById(R.id.num_02);
        officialNumTvs[2] = findViewById(R.id.num_03);
        officialNumTvs[3] = findViewById(R.id.num_04);
        officialNumTvs[4] = findViewById(R.id.num_05);
        officialNumTvs[5] = findViewById(R.id.num_06);

        // User UI
        userLetter = findViewById(R.id.english_letter_user);
        userNumTvs[0] = findViewById(R.id.num_01_user);
        userNumTvs[1] = findViewById(R.id.num_02_user);
        userNumTvs[2] = findViewById(R.id.num_03_user);
        userNumTvs[3] = findViewById(R.id.num_04_user);
        userNumTvs[4] = findViewById(R.id.num_05_user);
        userNumTvs[5] = findViewById(R.id.num_06_user);

        // Indicators
        officialLetterIndicator = findViewById(R.id.english_letter_indicator);
        userLetterIndicator = findViewById(R.id.english_letter_user_indicator);
        officialNumIndicators[0] = findViewById(R.id.num_01_indicator);
        officialNumIndicators[1] = findViewById(R.id.num_02_indicator);
        officialNumIndicators[2] = findViewById(R.id.num_03_indicator);
        officialNumIndicators[3] = findViewById(R.id.num_04_indicator);
        officialNumIndicators[4] = findViewById(R.id.num_05_indicator);
        officialNumIndicators[5] = findViewById(R.id.num_06_indicator);
        userNumIndicators[0] = findViewById(R.id.num_01_user_indicator);
        userNumIndicators[1] = findViewById(R.id.num_02_user_indicator);
        userNumIndicators[2] = findViewById(R.id.num_03_user_indicator);
        userNumIndicators[3] = findViewById(R.id.num_04_user_indicator);
        userNumIndicators[4] = findViewById(R.id.num_05_user_indicator);
        userNumIndicators[5] = findViewById(R.id.num_06_user_indicator);

        if (viewPriceButton != null) {
            viewPriceButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click_animation));
                Intent intent = new Intent(this, ViewPriceActivity.class);
                matchResults.addToIntent(intent, "Mahajana Sampatha");
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
        updateMatchIndicators();
    }

    private void updateOfficialDisplay(OfficialResults official) {
        logoImageView.setImageResource(R.drawable.mahajana_sampatha);
        lotteryNameTextView.setText("Mahajana Sampatha");
        if (official.drawDate != null) {
            dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(official.drawDate.toDate()));
        }
        drawNoTextView.setText(official.drawNo != null ? "Draw #" + official.drawNo : "N/A");
        officialLetter.setText(official.letter);

        if (official.sixDigitNumber != null && official.sixDigitNumber.length() == 6) {
            for (int i = 0; i < 6; i++) {
                officialNumTvs[i].setText(String.valueOf(official.sixDigitNumber.charAt(i)));
            }
        }
    }

    private void updateUserDisplay(UserResults user) {
        userLetter.setText(user.letter);
        if (user.sixDigitNumber != null && user.sixDigitNumber.length() == 6) {
            for (int i = 0; i < 6; i++) {
                userNumTvs[i].setText(String.valueOf(user.sixDigitNumber.charAt(i)));
            }
        }
    }

    private void compareResults(OfficialResults official, UserResults user) {
        if (official.letter == null || official.sixDigitNumber == null || user.letter == null || user.sixDigitNumber == null || official.sixDigitNumber.length() != 6 || user.sixDigitNumber.length() != 6) {
            return;
        }

        matchResults.isLetterMatched = official.letter.equals(user.letter);

        // 1. මුල සිට පිළිවෙලට ගැලපෙන ගණන සෙවීම (Forward Match)
        int forwardMatchCount = 0;
        for (int i = 0; i < 6; i++) {
            if (official.sixDigitNumber.charAt(i) == user.sixDigitNumber.charAt(i)) {
                forwardMatchCount++;
            } else {
                break;
            }
        }

        // 2. අග සිට පිළිවෙලට ගැලපෙන ගණන සෙවීම (Backward Match)
        int backwardMatchCount = 0;
        for (int i = 0; i < 6; i++) {
            if (official.sixDigitNumber.charAt(5 - i) == user.sixDigitNumber.charAt(5 - i)) {
                backwardMatchCount++;
            } else {
                break;
            }
        }

        // <<< CHANGE: ගැලපීම් ගණන MatchResults එකේ ගබඩා කිරීම >>>
        matchResults.forwardMatchCount = forwardMatchCount;
        matchResults.backwardMatchCount = backwardMatchCount;

        // 3. ත්‍යාගය සඳහා වැඩිම ගැලපෙන ගණන තෝරාගැනීම
        matchResults.matchedDigits = Math.max(forwardMatchCount, backwardMatchCount);
    }

    // <<< CHANGE: Indicators පෙන්වන සම්පූර්ණ logic එක වෙනස් කර ඇත >>>
    private void updateMatchIndicators() {
        setMatchImage(matchResults.isLetterMatched, officialLetterIndicator, userLetterIndicator);

        if (matchResults.forwardMatchCount >= matchResults.backwardMatchCount) {
            // Forward match එක විශාල නම් හෝ සමාන නම්, එයට අදාළව indicators පෙන්වයි
            for (int i = 0; i < 6; i++) {
                boolean isMatched = i < matchResults.forwardMatchCount;
                setMatchImage(isMatched, officialNumIndicators[i], userNumIndicators[i]);
            }
        } else {
            // Backward match එක විශාල නම්, එයට අදාළව indicators පෙන්වයි
            for (int i = 0; i < 6; i++) {
                boolean isMatched = i >= (6 - matchResults.backwardMatchCount);
                setMatchImage(isMatched, officialNumIndicators[i], userNumIndicators[i]);
            }
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
    private static class MatchResults {
        boolean isLetterMatched = false;
        int matchedDigits = 0;
        int forwardMatchCount = 0;
        int backwardMatchCount = 0;

        // <<< CHANGE: forward සහ backward counts Intent එකට එක් කරයි >>>
        void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra("LotteryName", lotteryName);
            intent.putExtra("IsLetterMatched", isLetterMatched);
            intent.putExtra("MatchedDigits", matchedDigits); // මෙය තවදුරටත් අවශ්‍ය නොවිය හැක, නමුත් ගැළපීම සඳහා තබමු
            intent.putExtra("ForwardMatchCount", forwardMatchCount);
            intent.putExtra("BackwardMatchCount", backwardMatchCount);
        }
    }

    private static class OfficialResults {
        Timestamp drawDate;
        Long drawNo;
        String letter, sixDigitNumber;

        OfficialResults(DocumentSnapshot doc) {
            this.drawDate = doc.getTimestamp("Date");
            this.drawNo = doc.getLong("Draw_No");
            this.letter = doc.getString("English_Letter");
            this.sixDigitNumber = doc.getString("Winning_Number");
        }
    }

    private static class UserResults {
        String letter, sixDigitNumber;

        UserResults(QRParser parser) {
            this.letter = parser.getSingleLetter();
            this.sixDigitNumber = parser.getSixDigitNumber();
        }
    }
}