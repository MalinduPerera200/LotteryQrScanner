package lk.jiat.qr_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ViewPriceActivity extends AppCompatActivity {

    private TextView tvLotteryName, tvPrizeAmount;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_price);

        initializeViews();

        Intent intent = getIntent();
        String lotteryName = intent.getStringExtra("LotteryName");

        int matchedNumbersCount = intent.getIntExtra("MatchedNumbersCount", 0);

        boolean isLetter1Matched = intent.getBooleanExtra("IsLetter1Matched", false);
        boolean isLetter2Matched = intent.getBooleanExtra("IsLetter2Matched", false);
        boolean is5DigitMatched = intent.getBooleanExtra("Is5DigitMatched", false);

        boolean isBonusMatched = intent.getBooleanExtra("IsBonusMatched", false);
        String nlbJayaMatchType = intent.getStringExtra("NlbJayaMatchType");
        int nlbForwardMatchCount = intent.getIntExtra("NlbForwardMatchCount", 0);
        int nlbBackwardMatchCount = intent.getIntExtra("NlbBackwardMatchCount", 0);


        tvLotteryName.setText(lotteryName);

        calculateAndDisplayPrize(lotteryName, matchedNumbersCount, isLetter1Matched, isLetter2Matched, is5DigitMatched, isBonusMatched, nlbJayaMatchType, nlbForwardMatchCount, nlbBackwardMatchCount);

        btnBack.setOnClickListener(v -> {
            Intent mainIntent = new Intent(ViewPriceActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }

    private void initializeViews() {
        tvLotteryName = findViewById(R.id.tvLotteryName);
        tvPrizeAmount = findViewById(R.id.tvPrizeAmount);
        btnBack = findViewById(R.id.btnBack);
    }

    private void calculateAndDisplayPrize(String lotteryName, int matchedCount, boolean letter1Matched, boolean letter2Matched, boolean fiveDigitMatched, boolean bonusMatched, String nlbJayaMatchType, int forwardCount, int backwardCount) {
        String prizeMessage = "Sorry, Better Luck Next Time!";
        boolean anyLetterMatched = letter1Matched || letter2Matched;

        switch (lotteryName) {
            case "Mega Power":
                if (matchedCount == 4 && letter1Matched && bonusMatched) prizeMessage = "Super Price";
                else if (matchedCount == 4 && letter1Matched) prizeMessage = "Rs. 10,000,000";
                else if (matchedCount == 4 && bonusMatched) prizeMessage = "Rs. 10,000,000";
                else if (matchedCount == 4) prizeMessage = "Rs. 2,000,000";
                else if (matchedCount == 3 && letter1Matched && bonusMatched) prizeMessage = "Rs. 200,040";
                else if (matchedCount == 3 && letter1Matched) prizeMessage = "Rs. 200,000";
                else if (matchedCount == 3 && bonusMatched) prizeMessage = "Rs. 5,040";
                else if (matchedCount == 3) prizeMessage = "Rs. 5,000";
                else if (matchedCount == 2 && letter1Matched && bonusMatched) prizeMessage = "Rs. 2,040";
                else if (matchedCount == 2 && letter1Matched) prizeMessage = "Rs. 2,000";
                else if (matchedCount == 2) prizeMessage = "Rs. 200";
                else if (matchedCount == 1 && letter1Matched && bonusMatched) prizeMessage = "Rs. 240";
                else if (matchedCount == 1 && letter1Matched) prizeMessage = "Rs. 200";
                else if (matchedCount == 1 && bonusMatched) prizeMessage = "Rs. 80";
                else if (matchedCount == 1 ) prizeMessage = "Rs. 40";
                else if (bonusMatched) prizeMessage = "Rs. 40";
                else if (letter1Matched) prizeMessage = "Rs. 40";
                break;

            case "Dhana Nidhanaya":
                if (matchedCount == 4 && letter1Matched) prizeMessage = "Super Prize!";
                else if (matchedCount == 4 && letter2Matched && fiveDigitMatched) prizeMessage = "Rs. 2,100,040";
                else if (matchedCount == 4 && letter2Matched) prizeMessage = "Rs. 2,000,040";
                else if (matchedCount == 4 && fiveDigitMatched) prizeMessage = "Rs. 2,100,000";
                else if (matchedCount == 4) prizeMessage = "Rs. 2,000,000";
                else if (matchedCount == 3 && letter1Matched && fiveDigitMatched) prizeMessage = "Rs. 300,000";
                else if (matchedCount == 3 && letter1Matched) prizeMessage = "Rs. 200,000";
                else if (matchedCount == 3 && letter2Matched && fiveDigitMatched) prizeMessage = "Rs. 106,040";
                else if (matchedCount == 3 && letter2Matched) prizeMessage = "Rs. 6,040";
                else if (matchedCount == 3 && fiveDigitMatched) prizeMessage = "Rs. 106,000";
                else if (matchedCount == 3) prizeMessage = "Rs. 6,000";
                else if (matchedCount == 2 && letter1Matched && fiveDigitMatched) prizeMessage = "Rs. 102,000";
                else if (matchedCount == 2 && letter1Matched) prizeMessage = "Rs. 2,000";
                else if (matchedCount == 2 && letter2Matched && fiveDigitMatched) prizeMessage = "Rs. 100,240";
                else if (matchedCount == 2 && letter2Matched) prizeMessage = "Rs. 240";
                else if (matchedCount == 2 && fiveDigitMatched) prizeMessage = "Rs. 100,200";
                else if (matchedCount == 2) prizeMessage = "Rs. 200";
                else if (matchedCount == 1 && letter1Matched && letter2Matched && fiveDigitMatched) prizeMessage = "Rs. 100,160";
                else if (matchedCount == 1 && letter1Matched && letter2Matched) prizeMessage = "Rs. 160";
                else if (matchedCount == 1 && letter1Matched && fiveDigitMatched) prizeMessage = "Rs. 100,120";
                else if (matchedCount == 1 && letter1Matched) prizeMessage = "Rs. 120";
                else if (matchedCount == 1 && letter2Matched && fiveDigitMatched) prizeMessage = "Rs. 100,080";
                else if (matchedCount == 1 && letter2Matched) prizeMessage = "Rs. 80";
                else if (fiveDigitMatched && matchedCount == 1) prizeMessage = "Rs. 100,040";
                else if (matchedCount == 1) prizeMessage = "Rs. 40";
                else if (fiveDigitMatched && anyLetterMatched) prizeMessage = "Rs. 100,040";
                else if (anyLetterMatched) prizeMessage = "Rs. 40";
                else if (fiveDigitMatched) prizeMessage = "Rs. 100,000";
                break;

            case "NLB Jaya":
                if (nlbJayaMatchType != null && nlbJayaMatchType.equals("BACKWARD")) {
                    if (backwardCount == 4 && letter1Matched) prizeMessage = "Rs. 500,000";
                    else if (backwardCount == 4) prizeMessage = "Rs. 50,000";
                    else if (backwardCount == 3 && letter1Matched) prizeMessage = "Rs. 2,040";
                    else if (backwardCount == 3) prizeMessage = "Rs. 2,000";
                    else if (backwardCount == 2 && letter1Matched) prizeMessage = "Rs. 240";
                    else if (backwardCount == 2) prizeMessage = "Rs. 200";
                    else if (backwardCount == 1 && letter1Matched) prizeMessage = "Rs. 80";
                    else if (backwardCount == 1) prizeMessage = "Rs. 40";
                }
                else if (nlbJayaMatchType != null && nlbJayaMatchType.equals("FORWARD")) {
                    if (forwardCount == 4) prizeMessage = "Rs. 1,000,000";
                    else if (forwardCount == 3 && letter1Matched) prizeMessage = "Rs. 240";
                    else if (forwardCount == 3) prizeMessage = "Rs. 200";
                    else if (forwardCount == 2 && letter1Matched) prizeMessage = "Rs. 120";
                    else if (forwardCount == 2) prizeMessage = "Rs. 80";
                    else if (forwardCount == 1 && letter1Matched) prizeMessage = "Rs. 80";
                    else if (forwardCount == 1) prizeMessage = "Rs. 40";
                    else if (!letter1Matched) prizeMessage = "Rs. 40";
            }else
                break;
        }

        tvPrizeAmount.setText(prizeMessage);
    }
}