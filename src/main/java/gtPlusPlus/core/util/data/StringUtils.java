package gtPlusPlus.core.util.data;

import java.util.ArrayList;

import gregtech.api.util.CustomGlyphs;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.core.util.Utils;

public class StringUtils {

    public static String superscript(String str) {
        str = str.replaceAll("0", CustomGlyphs.SUPERSCRIPT0);
        str = str.replaceAll("1", CustomGlyphs.SUPERSCRIPT1);
        str = str.replaceAll("2", CustomGlyphs.SUPERSCRIPT2);
        str = str.replaceAll("3", CustomGlyphs.SUPERSCRIPT3);
        str = str.replaceAll("4", CustomGlyphs.SUPERSCRIPT4);
        str = str.replaceAll("5", CustomGlyphs.SUPERSCRIPT5);
        str = str.replaceAll("6", CustomGlyphs.SUPERSCRIPT6);
        str = str.replaceAll("7", CustomGlyphs.SUPERSCRIPT7);
        str = str.replaceAll("8", CustomGlyphs.SUPERSCRIPT8);
        str = str.replaceAll("9", CustomGlyphs.SUPERSCRIPT9);
        return str;
    }

    public static String subscript(String str) {
        str = str.replaceAll("0", CustomGlyphs.SUBSCRIPT0);
        str = str.replaceAll("1", "\u2081");
        str = str.replaceAll("2", "\u2082");
        str = str.replaceAll("3", "\u2083");
        str = str.replaceAll("4", "\u2084");
        str = str.replaceAll("5", "\u2085");
        str = str.replaceAll("6", "\u2086");
        str = str.replaceAll("7", "\u2087");
        str = str.replaceAll("8", "\u2088");
        str = str.replaceAll("9", "\u2089");
        str = str.replaceAll("\\?", CustomGlyphs.SUBSCRIPT_QUESTION_MARK);
        return str;
    }

    public static boolean containsSuperOrSubScript(final String s) {
        if (s.contains(StringUtils.superscript("0"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("1"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("2"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("3"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("4"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("5"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("6"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("7"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("8"))) {
            return true;
        } else if (s.contains(StringUtils.superscript("9"))) {
            return true;
        }
        if (s.contains(StringUtils.subscript("0"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("1"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("2"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("3"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("4"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("5"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("6"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("7"))) {
            return true;
        } else if (s.contains(StringUtils.subscript("8"))) {
            return true;
        } else {
            return s.contains(StringUtils.subscript("9"));
        }
    }

    public static String firstLetterCaps(String data) {
        String firstLetter = data.substring(0, 1)
            .toUpperCase();
        String restLetters = data.substring(1)
            .toLowerCase();
        return firstLetter + restLetters;
    }

    public static <V> String getDataStringFromArray(V[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return "empty/null";
        } else {
            StringBuilder aData = new StringBuilder();
            for (V y : parameterTypes) {
                if (y != null) {
                    aData.append(", ")
                        .append(y);
                }
            }
            return aData.toString();
        }
    }

    /**
     * Is this a special regex character for delimination? (.$|()[]{}^?*+\\)
     *
     * @param aChar - The char to test
     * @return - Is this a special character?
     */
    public static boolean isSpecialCharacter(char aChar) {
        return aChar == '"' || aChar == '.'
            || aChar == '$'
            || aChar == '|'
            || aChar == '('
            || aChar == ')'
            || aChar == '['
            || aChar == ']'
            || aChar == '{'
            || aChar == '}'
            || aChar == '^'
            || aChar == '?'
            || aChar == '*'
            || aChar == '+'
            || aChar == '\\';
    }

    public static boolean isEscaped(String aString) {
        return aString.charAt(0) == '\\';
    }

    public static String splitAndUppercase(String aInput, String aDelim) {

        if (!isEscaped(aDelim)) {
            boolean isSpecial = false;
            for (int o = 0; o < aInput.length(); o++) {
                if (isSpecialCharacter(aInput.charAt(o))) {
                    isSpecial = true;
                    break;
                }
            }
            if (isSpecial) {
                aDelim = "\\" + aDelim;
            }
        }

        Logger.INFO("Splitting " + aInput);
        String[] aSplit = aInput.split(aDelim);
        Logger.INFO(aSplit.length + " parts.");
        if (aSplit.length == 0) {
            return aInput;
        } else {
            ArrayList<String> aTemp = new ArrayList<>();
            for (String s : aSplit) {
                Logger.INFO("Found: " + s);
                s = s.replace(".", "");
                s = Utils.sanitizeString(s);
                s = firstLetterCaps(s);
                Logger.INFO("Formatted & Captilized: " + s);
                aTemp.add(s);
            }
            Logger.INFO("Rebuilding");
            StringBuilder aReturn = new StringBuilder();
            for (String s : aTemp) {
                aReturn.append(s);
                Logger.INFO("Step: " + aReturn);
            }
            return aReturn.toString();
        }
    }

    public static long uppercaseCount(String aString) {
        return aString.chars()
            .filter(Character::isUpperCase)
            .count();
    }
}
