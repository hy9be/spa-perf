package com.hy9be.spaperf.util;

import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hyou on 6/23/15.
 */
public class RegExpWrapper {

    public static List<String> firstMatch(JsonObject regExp, String input) {
        List<String> allMatches = new ArrayList<>();
        // Reset multimatch regex state
        regExp.set("lastIndex", 0);
        Matcher m = Pattern.compile(regExp.get("").asString()).matcher(input);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    public static JsonObject matcher(JsonObject regExp, String input) {
        // Reset regex state for the case
        // someone did not loop over all matches
        // last time.
        regExp.set("lastIndex", 0);
        return new JsonObject().add("re", regExp).add("input", input);
    }
}
