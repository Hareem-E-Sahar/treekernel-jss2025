package sample.evaluation.util;

public class Preprocessor {
    public static String removeWrapper(String queryContent) {

        int firstBrace = queryContent.indexOf('{');

        // Find matching closing brace of the outer class
        int brace = 0;
        int end = -1;
        char[] chars = queryContent.toCharArray();

        for (int i = firstBrace; i < chars.length; i++) {
            if (chars[i] == '{') brace++;
            else if (chars[i] == '}') brace--;

            if (brace == 0) {
                end = i;
                break;
            }
        }

        String insideStr = queryContent.substring(firstBrace + 1, end).trim();
        return insideStr;
    }
}
