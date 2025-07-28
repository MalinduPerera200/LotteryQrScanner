package lk.jiat.qr_scanner;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Color-Based Match System
 * Indicators remove කරලා, number ball colors වෙනස් කරන system එක
 */
public class ColorBasedMatchSystem {

    /**
     * Match result with color information
     */
    public static class ColorMatchResult {
        public boolean isLetterMatched = false;
        public boolean isBonusMatched = false;
        public int matchedNumbersCount = 0;
        public List<Integer> officialMatchPositions = new ArrayList<>();
        public List<Integer> userMatchPositions = new ArrayList<>();
        public Set<String> matchedNumbers = new HashSet<>();
    }

    /**
     * Color constants for different states
     */
    public static class MatchColors {
        // Matched colors (Green theme)
        public static final int MATCHED_LETTER_COLOR = R.color.match_green_dark;      // Dark green for letters
        public static final int MATCHED_BONUS_COLOR = R.color.match_green_medium;     // Medium green for bonus
        public static final int MATCHED_NUMBER_COLOR = R.color.match_green_light;     // Light green for numbers

        // Unmatched colors (Original theme)
        public static final int UNMATCHED_LETTER_COLOR = R.color.primary;             // Blue for letters
        public static final int UNMATCHED_BONUS_COLOR = R.color.bonus_ball_color;     // Orange for bonus
        public static final int UNMATCHED_NUMBER_COLOR = R.color.number_ball_unmatched; // Gray for numbers

        // Text colors
        public static final int MATCHED_TEXT_COLOR = R.color.white;
        public static final int UNMATCHED_TEXT_COLOR = R.color.text_primary;
    }

    /**
     * Update Mega Power match indicators using color system
     */
    public static ColorMatchResult updateMegaPowerWithColors(
            String[] officialNumbers,  // ["R", "19", "1", "10", "47", "58"]
            String[] userNumbers,      // ["N", "21", "10", "18", "67", "77"]
            CardView[] officialCards,  // CardView references
            CardView[] userCards,
            ImageView[] officialIndicators, // To hide these
            ImageView[] userIndicators,     // To hide these
            android.content.Context context) {

        ColorMatchResult result = new ColorMatchResult();

        // 1. Hide all indicators first
        hideAllIndicators(officialIndicators);
        hideAllIndicators(userIndicators);

        // 2. Letter comparison (first position)
        if (officialNumbers.length > 0 && userNumbers.length > 0) {
            result.isLetterMatched = officialNumbers[0].equals(userNumbers[0]);

            // Set letter colors
            setCardColor(context, officialCards[0],
                    result.isLetterMatched ? MatchColors.MATCHED_LETTER_COLOR : MatchColors.UNMATCHED_LETTER_COLOR);
            setCardColor(context, userCards[0],
                    result.isLetterMatched ? MatchColors.MATCHED_LETTER_COLOR : MatchColors.UNMATCHED_LETTER_COLOR);
        }

        // 3. Check if bonus exists and compare
        boolean hasBonus = officialNumbers.length >= 6 && userNumbers.length >= 6;
        if (hasBonus) {
            result.isBonusMatched = officialNumbers[1].equals(userNumbers[1]);

            // Set bonus colors
            setCardColor(context, officialCards[1],
                    result.isBonusMatched ? MatchColors.MATCHED_BONUS_COLOR : MatchColors.UNMATCHED_BONUS_COLOR);
            setCardColor(context, userCards[1],
                    result.isBonusMatched ? MatchColors.MATCHED_BONUS_COLOR : MatchColors.UNMATCHED_BONUS_COLOR);
        }

        // 4. Numbers comparison (positions after letter and bonus)
        int startIndex = hasBonus ? 2 : 1;
        int endIndex = hasBonus ? 6 : 5;

        // Create sets for efficient comparison
        Set<String> officialNumbersSet = new HashSet<>();
        Set<String> userNumbersSet = new HashSet<>();

        // Add numbers to sets
        for (int i = startIndex; i < Math.min(officialNumbers.length, endIndex); i++) {
            officialNumbersSet.add(officialNumbers[i]);
        }
        for (int i = startIndex; i < Math.min(userNumbers.length, endIndex); i++) {
            userNumbersSet.add(userNumbers[i]);
        }

        // 5. Check each official number and set colors
        for (int i = startIndex; i < Math.min(officialNumbers.length, endIndex); i++) {
            String officialNum = officialNumbers[i];
            boolean isMatched = userNumbersSet.contains(officialNum);

            if (isMatched) {
                result.matchedNumbersCount++;
                result.matchedNumbers.add(officialNum);
                result.officialMatchPositions.add(i);
            }

            // Set color based on match
            setCardColor(context, officialCards[i],
                    isMatched ? MatchColors.MATCHED_NUMBER_COLOR : MatchColors.UNMATCHED_NUMBER_COLOR);
        }

        // 6. Check each user number and set colors
        for (int i = startIndex; i < Math.min(userNumbers.length, endIndex); i++) {
            String userNum = userNumbers[i];
            boolean isMatched = officialNumbersSet.contains(userNum);

            if (isMatched) {
                result.userMatchPositions.add(i);
            }

            // Set color based on match
            setCardColor(context, userCards[i],
                    isMatched ? MatchColors.MATCHED_NUMBER_COLOR : MatchColors.UNMATCHED_NUMBER_COLOR);
        }

        return result;
    }

    /**
     * Helper method to hide all indicators
     */
    private static void hideAllIndicators(ImageView[] indicators) {
        for (ImageView indicator : indicators) {
            if (indicator != null) {
                indicator.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Helper method to set card background color
     */
    private static void setCardColor(android.content.Context context, CardView card, int colorRes) {
        if (card != null && context != null) {
            int color = ContextCompat.getColor(context, colorRes);
            card.setCardBackgroundColor(ColorStateList.valueOf(color));
        }
    }

    /**
     * Alternative method using direct color values
     */
    public static void setCardColorDirect(CardView card, int color) {
        if (card != null) {
            card.setCardBackgroundColor(ColorStateList.valueOf(color));
        }
    }

    /**
     * Method to get color based on match status
     */
    public static int getMatchColor(android.content.Context context, boolean isMatched, String type) {
        if (isMatched) {
            switch (type.toLowerCase()) {
                case "letter":
                    return ContextCompat.getColor(context, MatchColors.MATCHED_LETTER_COLOR);
                case "bonus":
                    return ContextCompat.getColor(context, MatchColors.MATCHED_BONUS_COLOR);
                case "number":
                default:
                    return ContextCompat.getColor(context, MatchColors.MATCHED_NUMBER_COLOR);
            }
        } else {
            switch (type.toLowerCase()) {
                case "letter":
                    return ContextCompat.getColor(context, MatchColors.UNMATCHED_LETTER_COLOR);
                case "bonus":
                    return ContextCompat.getColor(context, MatchColors.UNMATCHED_BONUS_COLOR);
                case "number":
                default:
                    return ContextCompat.getColor(context, MatchColors.UNMATCHED_NUMBER_COLOR);
            }
        }
    }

    /**
     * Debug method to log color match results
     */
    public static void logColorMatchResults(ColorMatchResult result, String lotteryType) {
        android.util.Log.d("ColorMatch", "=== " + lotteryType + " Color Match Results ===");
        android.util.Log.d("ColorMatch", "Letter matched: " + result.isLetterMatched);
        android.util.Log.d("ColorMatch", "Bonus matched: " + result.isBonusMatched);
        android.util.Log.d("ColorMatch", "Numbers matched: " + result.matchedNumbersCount);
        android.util.Log.d("ColorMatch", "Matched numbers: " + result.matchedNumbers);
        android.util.Log.d("ColorMatch", "Official positions: " + result.officialMatchPositions);
        android.util.Log.d("ColorMatch", "User positions: " + result.userMatchPositions);
    }

    /**
     * Animate color change (optional)
     */
    public static void animateColorChange(CardView card, int fromColor, int toColor) {
        if (card != null) {
            android.animation.ValueAnimator colorAnimation = android.animation.ValueAnimator.ofArgb(fromColor, toColor);
            colorAnimation.setDuration(300); // 300ms animation
            colorAnimation.addUpdateListener(animator -> {
                int animatedColor = (int) animator.getAnimatedValue();
                card.setCardBackgroundColor(ColorStateList.valueOf(animatedColor));
            });
            colorAnimation.start();
        }
    }
}