package lk.jiat.qr_scanner.constants;

import android.widget.Toast;

import lk.jiat.qr_scanner.R;

/**
 * Centralized constants for the lottery application
 * Eliminates hardcoded strings and magic numbers throughout the codebase
 */
public final class LotteryConstants {

    // Prevent instantiation
    private LotteryConstants() {}

    /**
     * Lottery Types
     */
    public static final class LotteryTypes {
        public static final String MEGA_POWER = "Mega Power";
        public static final String NLB_JAYA = "NLB Jaya";
        public static final String GOVI_SETHA = "Govi Setha";
        public static final String DHANA_NIDHANAYA = "Dhana Nidhanaya";
        public static final String MAHAJANA_SAMPATHA = "Mahajana Sampatha";
    }

    /**
     * Firestore Collection Names
     */
    public static final class Collections {
        public static final String MEGA_POWER = "Mega_Power";
        public static final String NLB_JAYA = "NLB_Jaya";
        public static final String GOVI_SETHA = "Govi_Setha";
        public static final String DHANA_NIDHANAYA = "Dhana_Nidhanaya";
        public static final String MAHAJANA_SAMPATHA = "Mahajana_Sampatha";
    }

    /**
     * Firestore Field Names
     */
    public static final class FirestoreFields {
        public static final String DRAW_NO = "Draw_No";
        public static final String DATE = "Date";
        public static final String ENGLISH_LETTER = "English_Letter";
        public static final String ENGLISH_LETTER_2 = "English_Letter_2";
        public static final String BONUS_NUMBER = "Bonus_Number";
        public static final String SPECIAL_NUMBER = "Special_Number";
        public static final String WINNING_NUMBER = "Winning_Number";
        public static final String SPECIAL_DRAWS = "Special_Draws";
        public static final String LAKSHAPATHI_DOUBLE_CHANCE_NO = "Lakshapathi_Double_Chance_No";

        // Number fields
        public static final String NUMBER_01 = "Number01";
        public static final String NUMBER_02 = "Number02";
        public static final String NUMBER_03 = "Number03";
        public static final String NUMBER_04 = "Number04";
    }

    /**
     * Intent Extra Keys
     */
    public static final class IntentExtras {
        public static final String QR_RESULT = "QrResult";
        public static final String LOTTERY_NAME = "LotteryName";
        public static final String IS_LETTER_MATCHED = "IsLetterMatched";
        public static final String IS_LETTER2_MATCHED = "IsLetter2Matched";
        public static final String IS_5DIGIT_MATCHED = "Is5DigitMatched";
        public static final String IS_BONUS_MATCHED = "IsBonusMatched";
        public static final String MATCHED_NUMBERS_COUNT = "MatchedNumbersCount";
        public static final String MATCHED_NUMBERS_LIST = "MatchedNumbersList";
        public static final String MATCHED_DIGITS = "MatchedDigits";
        public static final String FORWARD_MATCH_COUNT = "ForwardMatchCount";
        public static final String BACKWARD_MATCH_COUNT = "BackwardMatchCount";
        public static final String NLB_JAYA_MATCH_TYPE = "NlbJayaMatchType";
        public static final String NLB_FORWARD_MATCH_COUNT = "NlbForwardMatchCount";
        public static final String NLB_BACKWARD_MATCH_COUNT = "NlbBackwardMatchCount";
    }

    /**
     * Animation and UI Constants
     */
    public static final class UI {
        public static final int LOADING_ANIMATION_REPEAT_COUNT = -1; // Infinite
        public static final float LOADING_ANIMATION_SPEED = 1.0f;
        public static final int TOAST_DURATION_ERROR = Toast.LENGTH_LONG;
        public static final int TOAST_DURATION_SUCCESS = Toast.LENGTH_SHORT;
    }

    /**
     * Validation Constants
     */
    public static final class Validation {
        public static final int MAX_QR_LENGTH = 500;
        public static final int MIN_QR_LENGTH = 10;
        public static final int MIN_DRAW_NUMBER_LENGTH = 4;
        public static final int MAX_DRAW_NUMBER_LENGTH = 8;
        public static final long MIN_DRAW_NUMBER = 1;
        public static final long MAX_DRAW_NUMBER = 99999999L;
    }

    /**
     * Match Types for NLB Jaya
     */
    public static final class MatchTypes {
        public static final String FORWARD = "FORWARD";
        public static final String BACKWARD = "BACKWARD";
        public static final String NONE = "NONE";
    }

    /**
     * Date Format Patterns
     */
    public static final class DateFormats {
        public static final String DISPLAY_FORMAT = "MMM dd, yyyy";
        public static final String FULL_FORMAT = "MMMM dd, yyyy";
    }

    /**
     * Prize Categories
     */
    public static final class Prizes {
        public static final String SUPER_PRIZE = "Super Prize!";
        public static final String JACKPOT = "Jackpot Winner!";
        public static final String BETTER_LUCK = "Sorry, Better Luck Next Time!";
    }

    /**
     * Lottery Configuration Mapping
     */
    public static final class LotteryConfig {

        public static String getCollectionName(String lotteryType) {
            switch (lotteryType) {
                case LotteryTypes.MEGA_POWER:
                    return Collections.MEGA_POWER;
                case LotteryTypes.NLB_JAYA:
                    return Collections.NLB_JAYA;
                case LotteryTypes.GOVI_SETHA:
                    return Collections.GOVI_SETHA;
                case LotteryTypes.DHANA_NIDHANAYA:
                    return Collections.DHANA_NIDHANAYA;
                case LotteryTypes.MAHAJANA_SAMPATHA:
                    return Collections.MAHAJANA_SAMPATHA;
                default:
                    return null;
            }
        }

        public static int getLogoResource(String lotteryType) {
            switch (lotteryType) {
                case LotteryTypes.MEGA_POWER:
                    return R.drawable.mega_logo;
                case LotteryTypes.NLB_JAYA:
                    return R.drawable.nlb_jaya_logo;
                case LotteryTypes.GOVI_SETHA:
                    return R.drawable.govisetha;
                case LotteryTypes.DHANA_NIDHANAYA:
                    return R.drawable.dana_nidanaya;
                case LotteryTypes.MAHAJANA_SAMPATHA:
                    return R.drawable.mahajana_sampatha;
                default:
                    return R.drawable.ic_launcher_foreground; // Default fallback
            }
        }
    }
}
