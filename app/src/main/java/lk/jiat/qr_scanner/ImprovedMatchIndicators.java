package lk.jiat.qr_scanner;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Improved Match Indicators System
 * ටික් සහ ක්‍රොස් මාර්ක් හරියට පෙන්වන system එකක්
 */
public class ImprovedMatchIndicators {

    /**
     * Match result holder class
     */
    public static class MatchResult {
        public boolean isLetterMatched = false;
        public boolean isBonusMatched = false;
        public int matchedNumbersCount = 0;
        public List<Integer> officialMatchPositions = new ArrayList<>();  // කොන් positions match වුණද
        public List<Integer> userMatchPositions = new ArrayList<>();      // user positions match වුණද
        public Set<String> matchedNumbers = new HashSet<>();              // match වුණ numbers
    }

    /**
     * Mega Power සඳහා match indicators update කරන method
     */
    public static MatchResult updateMegaPowerMatchIndicators(
            String[] officialNumbers,  // ["R", "19", "1", "10", "47", "58"]
            String[] userNumbers,      // ["N", "21", "10", "18", "67", "77"]
            ImageView[] officialIndicators,
            ImageView[] userIndicators,
            TextView[] officialTextViews,
            TextView[] userTextViews) {

        MatchResult result = new MatchResult();

        // Reset all indicators පළමුව
        resetAllIndicators(officialIndicators);
        resetAllIndicators(userIndicators);

        // 1. Letter comparison (first position)
        if (officialNumbers.length > 0 && userNumbers.length > 0) {
            result.isLetterMatched = officialNumbers[0].equals(userNumbers[0]);
            setIndicator(officialIndicators[0], result.isLetterMatched);
            setIndicator(userIndicators[0], result.isLetterMatched);
        }

        // 2. Numbers comparison (positions 1-5, excluding bonus if exists)
        // Mega Power structure: [Letter, Bonus, Num1, Num2, Num3, Num4] or [Letter, Num1, Num2, Num3, Num4]

        boolean hasBonus = officialNumbers.length >= 6 && userNumbers.length >= 6;
        int startIndex = hasBonus ? 2 : 1;  // Skip letter and bonus if exists
        int endIndex = hasBonus ? 6 : 5;

        // Create sets for efficient comparison
        Set<String> officialNumbersSet = new HashSet<>();
        Set<String> userNumbersSet = new HashSet<>();

        // Add numbers to sets (skip letter and bonus)
        for (int i = startIndex; i < Math.min(officialNumbers.length, endIndex); i++) {
            officialNumbersSet.add(officialNumbers[i]);
        }
        for (int i = startIndex; i < Math.min(userNumbers.length, endIndex); i++) {
            userNumbersSet.add(userNumbers[i]);
        }

        // 3. Check bonus match (second position if exists)
        if (hasBonus) {
            result.isBonusMatched = officialNumbers[1].equals(userNumbers[1]);
            setIndicator(officialIndicators[1], result.isBonusMatched);
            setIndicator(userIndicators[1], result.isBonusMatched);
        }

        // 4. Check each official number
        for (int i = startIndex; i < Math.min(officialNumbers.length, endIndex); i++) {
            String officialNum = officialNumbers[i];
            boolean isMatched = userNumbersSet.contains(officialNum);

            if (isMatched) {
                result.matchedNumbersCount++;
                result.matchedNumbers.add(officialNum);
                result.officialMatchPositions.add(i);
            }

            setIndicator(officialIndicators[i], isMatched);
        }

        // 5. Check each user number
        for (int i = startIndex; i < Math.min(userNumbers.length, endIndex); i++) {
            String userNum = userNumbers[i];
            boolean isMatched = officialNumbersSet.contains(userNum);

            if (isMatched) {
                result.userMatchPositions.add(i);
            }

            setIndicator(userIndicators[i], isMatched);
        }

        return result;
    }

    /**
     * Helper method to reset all indicators
     */
    private static void resetAllIndicators(ImageView[] indicators) {
        for (ImageView indicator : indicators) {
            if (indicator != null) {
                indicator.setImageResource(R.drawable.close);
                indicator.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Helper method to set individual indicator
     */
    private static void setIndicator(ImageView indicator, boolean isMatched) {
        if (indicator != null) {
            indicator.setImageResource(isMatched ? R.drawable.done : R.drawable.close);
            indicator.setVisibility(View.VISIBLE);

            // Add content description for accessibility
            indicator.setContentDescription(isMatched ? "Number matched" : "Number not matched");
        }
    }

    /**
     * Generic method for any lottery type
     */
    public static MatchResult updateMatchIndicators(
            List<String> officialNumbers,
            List<String> userNumbers,
            String officialLetter,
            String userLetter,
            String officialBonus,
            String userBonus,
            ImageView[] officialIndicators,
            ImageView[] userIndicators,
            ImageView officialLetterIndicator,
            ImageView userLetterIndicator,
            ImageView officialBonusIndicator,
            ImageView userBonusIndicator) {

        MatchResult result = new MatchResult();

        // Reset indicators
        resetAllIndicators(officialIndicators);
        resetAllIndicators(userIndicators);

        // 1. Letter comparison
        if (officialLetter != null && userLetter != null) {
            result.isLetterMatched = officialLetter.equals(userLetter);
            setIndicator(officialLetterIndicator, result.isLetterMatched);
            setIndicator(userLetterIndicator, result.isLetterMatched);
        }

        // 2. Bonus comparison
        if (officialBonus != null && userBonus != null) {
            result.isBonusMatched = officialBonus.equals(userBonus);
            setIndicator(officialBonusIndicator, result.isBonusMatched);
            setIndicator(userBonusIndicator, result.isBonusMatched);
        }

        // 3. Numbers comparison
        Set<String> officialSet = new HashSet<>(officialNumbers);
        Set<String> userSet = new HashSet<>(userNumbers);

        // Check official numbers
        for (int i = 0; i < officialNumbers.size() && i < officialIndicators.length; i++) {
            String num = officialNumbers.get(i);
            boolean isMatched = userSet.contains(num);

            if (isMatched) {
                result.matchedNumbersCount++;
                result.matchedNumbers.add(num);
                result.officialMatchPositions.add(i);
            }

            setIndicator(officialIndicators[i], isMatched);
        }

        // Check user numbers
        for (int i = 0; i < userNumbers.size() && i < userIndicators.length; i++) {
            String num = userNumbers.get(i);
            boolean isMatched = officialSet.contains(num);

            if (isMatched) {
                result.userMatchPositions.add(i);
            }

            setIndicator(userIndicators[i], isMatched);
        }

        return result;
    }

    /**
     * Debug method to log match results
     */
    public static void logMatchResults(MatchResult result, String lotteryType) {
        android.util.Log.d("MatchIndicators", "=== " + lotteryType + " Match Results ===");
        android.util.Log.d("MatchIndicators", "Letter matched: " + result.isLetterMatched);
        android.util.Log.d("MatchIndicators", "Bonus matched: " + result.isBonusMatched);
        android.util.Log.d("MatchIndicators", "Numbers matched: " + result.matchedNumbersCount);
        android.util.Log.d("MatchIndicators", "Matched numbers: " + result.matchedNumbers);
        android.util.Log.d("MatchIndicators", "Official positions: " + result.officialMatchPositions);
        android.util.Log.d("MatchIndicators", "User positions: " + result.userMatchPositions);
    }
}