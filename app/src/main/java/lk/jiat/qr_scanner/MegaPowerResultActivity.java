package lk.jiat.qr_scanner;

import android.content.Intent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MegaPowerResultActivity extends BaseResultActivity {

    private TextView lotteryNameTextView, dateTextView, drawNoTextView;
    private TextView englishLetterTextView, bonusNumTextView;
    private TextView englishLetterTextViewUser, bonusNumTextViewUser;
    private final TextView[] officialNumTextViews = new TextView[4];
    private final TextView[] userNumTextViews = new TextView[4];
    private ImageView logoImageView;
    private final ImageView[] officialNumImageViews = new ImageView[4];
    private final ImageView[] userNumImageViews = new ImageView[4];
    private ImageView englishLetterImOfficial, englishLetterImUser, bonusNumImOfficial, bonusNumImUser;
    private CardView bonusNumberCard, bonusNumberCardUser;

    // Data Holders
    private MatchResults matchResults;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_mega_power_result;
    }

    @Override
    protected String getFirestoreCollectionName() {
        return "Mega_Power";
    }

    @Override
    protected void initializeViews() {
        matchResults = new MatchResults();

        // Logo, Name, Date, Draw
        logoImageView = findViewById(R.id.logo_image_view);
        lotteryNameTextView = findViewById(R.id.lottery_name);
        dateTextView = findViewById(R.id.date);
        drawNoTextView = findViewById(R.id.draw_no);

        // Official Winning Numbers UI
        englishLetterTextView = findViewById(R.id.english_letter);
        bonusNumTextView = findViewById(R.id.bonusNumber);
        officialNumTextViews[0] = findViewById(R.id.num_01);
        officialNumTextViews[1] = findViewById(R.id.num_02);
        officialNumTextViews[2] = findViewById(R.id.num_03);
        officialNumTextViews[3] = findViewById(R.id.num_05);

        // User's Numbers UI
        englishLetterTextViewUser = findViewById(R.id.english_letter_user);
        bonusNumTextViewUser = findViewById(R.id.bonus_num_user);
        userNumTextViews[0] = findViewById(R.id.num_05);
        userNumTextViews[1] = findViewById(R.id.num_06);
        userNumTextViews[2] = findViewById(R.id.num_07);
        userNumTextViews[3] = findViewById(R.id.num_08);

        // Match Indicator ImageViews (Official)
        englishLetterImOfficial = findViewById(R.id.english_letter_indicator2);
        bonusNumImOfficial = findViewById(R.id.english_letter_indicator3);
        officialNumImageViews[0] = findViewById(R.id.english_letter_indicator4);
        officialNumImageViews[1] = findViewById(R.id.english_letter_indicator5);
        officialNumImageViews[2] = findViewById(R.id.english_letter_indicator6);
        officialNumImageViews[3] = findViewById(R.id.english_letter_indicator7);

        // Match Indicator ImageViews (User)
        englishLetterImUser = findViewById(R.id.english_letter_indicator);
        bonusNumImUser = findViewById(R.id.bonus_num_indicator);
        userNumImageViews[0] = findViewById(R.id.num_05_indicator);
        userNumImageViews[1] = findViewById(R.id.num_06_indicator);
        userNumImageViews[2] = findViewById(R.id.num_07_indicator);
        userNumImageViews[3] = findViewById(R.id.num_08_indicator);

        // CardViews for visibility control
        bonusNumberCard = findViewById(R.id.bonusNumberCard);
        bonusNumberCardUser = findViewById(R.id.bonusNumberCard1);

        // Setup "View Price" button click listener
        if (viewPriceButton != null) {
            viewPriceButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click_animation));
                Intent intent = new Intent(this, ViewPriceActivity.class);
                // Pass all match results to the next activity
                matchResults.addToIntent(intent, "Mega Power");
                startActivity(intent);
            });
        }
    }

    @Override
    protected void displayAndCompareResults(DocumentSnapshot document, QRParser parser) {
        // 1. Parse official and user results into helper classes
        OfficialResults officialResults = new OfficialResults(document);
        UserResults userResults = new UserResults(parser.getWinningNumbers(), parser.getSingleLetter());

        // 2. Display the data on the UI
        updateOfficialDisplay(officialResults);
        updateUserDisplay(userResults);

        // 3. Compare the numbers and update match results
        compareNumbers(officialResults, userResults);

        // 4. Show match indicators (tick/cross) on the UI
        updateMatchIndicators(officialResults, userResults);
    }

    private void updateOfficialDisplay(OfficialResults official) {
        logoImageView.setImageResource(R.drawable.mega_logo);
        lotteryNameTextView.setText("Mega Power");

        if (official.drawDate != null) {
            dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(official.drawDate.toDate()));
        }
        drawNoTextView.setText(official.drawNo != null ? "Draw #" + official.drawNo : "N/A");
        englishLetterTextView.setText(official.engLetter != null ? official.engLetter : "N/A");

        // Bonus number visibility
        if (official.bonusNum != null) {
            bonusNumTextView.setText(String.valueOf(official.bonusNum));
            bonusNumberCard.setVisibility(View.VISIBLE);
        } else {
            bonusNumberCard.setVisibility(View.GONE);
        }

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

        for (int i = 0; i < userNumTextViews.length; i++) {
            if (i < user.numbersList.size()) {
                userNumTextViews[i].setText(String.valueOf(user.numbersList.get(i)));
            } else {
                userNumTextViews[i].setText("N/A");
            }
        }
    }

    private void compareNumbers(OfficialResults official, UserResults user) {
        // Compare English Letter
        matchResults.isLetterMatched = official.engLetter != null && official.engLetter.equals(user.letter);

        // Compare Bonus Number
        matchResults.isBonusMatched = official.bonusNum != null && official.bonusNum.equals(user.bonusNum);

        // Compare Regular Numbers
        Set<Long> officialNumbersSet = new HashSet<>(official.numbersList);
        for (Long userNum : user.numbersList) {
            if (officialNumbersSet.contains(userNum)) {
                matchResults.matchedNumbersCount++;
                matchResults.matchedNumbersList.add(userNum);
            }
        }
    }

    private void updateMatchIndicators(OfficialResults official, UserResults user) {
        setMatchImage(matchResults.isLetterMatched, englishLetterImOfficial, englishLetterImUser);
        setMatchImage(matchResults.isBonusMatched, bonusNumImOfficial, bonusNumImUser);

        Set<Long> officialNumbersSet = new HashSet<>(official.numbersList);
        Set<Long> userNumbersSet = new HashSet<>(user.numbersList);

        // Update indicators for official numbers row
        for (int i = 0; i < official.numbersList.size(); i++) {
            boolean isMatched = userNumbersSet.contains(official.numbersList.get(i));
            setMatchImage(isMatched, officialNumImageViews[i]);
        }

        // Update indicators for user numbers row
        for (int i = 0; i < user.numbersList.size(); i++) {
            boolean isMatched = officialNumbersSet.contains(user.numbersList.get(i));
            setMatchImage(isMatched, userNumImageViews[i]);
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

    // --- Helper Classes for Data Handling ---

    private static class MatchResults {
        boolean isLetterMatched = false;
        boolean isBonusMatched = false;
        int matchedNumbersCount = 0;
        ArrayList<Long> matchedNumbersList = new ArrayList<>();

        void addToIntent(Intent intent, String lotteryName) {
            intent.putExtra("LotteryName", lotteryName);
            intent.putExtra("IsLetterMatched", isLetterMatched);
            intent.putExtra("IsBonusMatched", isBonusMatched);
            intent.putExtra("MatchedNumbersCount", matchedNumbersCount);
            intent.putExtra("MatchedNumbersList", matchedNumbersList);
        }
    }

    private static class OfficialResults {
        Timestamp drawDate;
        Long drawNo;
        String engLetter;
        Long bonusNum;
        final List<Long> numbersList = new ArrayList<>();

        OfficialResults(DocumentSnapshot doc) {
            this.drawDate = doc.getTimestamp("Date");
            this.drawNo = doc.getLong("Draw_No");
            this.engLetter = doc.getString("English_Letter");
            this.bonusNum = doc.getLong("Bonus_Number");

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
        Long bonusNum;
        final List<Long> numbersList = new ArrayList<>();

        UserResults(List<String> qrNumbers, String qrLetter) {
            this.letter = qrLetter;

            // Mega Power has a bonus number, so the list size is >= 5
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
                    // Ignore if a number is invalid
                }
            }
        }
    }
}