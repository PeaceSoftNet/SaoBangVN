package net.peacesoft.commons;

/**
 * String utilities.
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class StringUtils {

    /**
     * Split string by delimeter
     *
     * @param original Original string
     * @param delimeter Delimeter string
     * @return Array string.
     */
    public static String[] split(String original, String delimeter) {
        String array_[] = new String[0];
        String value_ = original;
        while (true) {
            int i = value_.indexOf(delimeter);
            String copyValue = value_;
            if (i >= 0) {
                copyValue = value_.substring(0, i);
            }
            String copyArray[] = new String[array_.length + 1];
            System.arraycopy(array_, 0, copyArray, 0, array_.length);
            copyArray[copyArray.length - 1] = copyValue;
            array_ = copyArray;
            value_ = value_.substring(i + delimeter.length(), value_.length());
            if (value_.length() <= 0 || i < 0) {
                break;
            }
        }
        return array_;
    }

    public static String removeSpecialChar(String msg, char[] exception) {
        String str = msg;
        String sRet = "";
        if (str == null) {
            str = "";
        }
        int i = 0;
        int len = str.length();
        while (i < len) {
            if ((i < len)
                    && (isCharException(str.charAt(i), exception)
                    || ((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z'))
                    || ((str.charAt(i) >= '0') && (str.charAt(i) <= '9')) || ((str.charAt(i) >= 'a') && (str.charAt(i) <= 'z')))) {
                sRet = sRet + str.charAt(i);
                i++;
            } else {
                i++;
            }
        }
        return sRet;
    }

    private static boolean isCharException(char info, char[] exception) {
        if (exception == null) {
            return false;
        }
        for (int j = 0; j < exception.length; j++) {
            if (info == exception[j]) {
                return true;
            }
        }
        return false;
    }

    public static String removeSpecialChar(String msg) {
        String str = msg;
        String sRet = "";
        if (str == null) {
            str = "";
        }
        int i = 0;
        int len = str.length();
        while (i < len) {
            if ((i < len)
                    && ((str.charAt(i) == '-')
                    || (str.charAt(i) == '#')
                    || ((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z'))
                    || ((str.charAt(i) >= '0') && (str.charAt(i) <= '9')) || ((str.charAt(i) >= 'a') && (str.charAt(i) <= 'z')))) {
                sRet = sRet + str.charAt(i);
                i++;
            } else {
                i++;
            }
        }

        return sRet;
    }

    public static String removeSpecialCharException(String msg) {
        String str = msg;
        String sRet = "";
        if (str == null) {
            str = "";
        }
        int i = 0;
        int len = str.length();
        while (i < len) {
            if ((i < len)
                    && ((str.charAt(i) == ':')
                    || (str.charAt(i) == ' ')
                    || (str.charAt(i) == '-')
                    || (str.charAt(i) == '#')
                    || ((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z'))
                    || ((str.charAt(i) >= '0') && (str.charAt(i) <= '9')) || ((str.charAt(i) >= 'a') && (str.charAt(i) <= 'z')))) {
                sRet = sRet + str.charAt(i);
                i++;
            } else {
                i++;
            }
        }

        return sRet;
    }

    public static String removeSpecialCharEndLine(String msg) {
        String str = msg;
        String sRet = "";
        if (str == null) {
            str = "";
        }
        int i = 0;
        int len = str.length();
        while (i < len) {
            if ((i < len)
                    && ((str.charAt(i) == '\n')
                    || (str.charAt(i) == ':')
                    || (str.charAt(i) == ' ')
                    || (str.charAt(i) == '-')
                    || (str.charAt(i) == '#')
                    || ((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z'))
                    || ((str.charAt(i) >= '0') && (str.charAt(i) <= '9')) || ((str.charAt(i) >= 'a') && (str.charAt(i) <= 'z')))) {
                sRet = sRet + str.charAt(i);
                i++;
            } else {
                i++;
            }
        }

        return sRet;
    }
}