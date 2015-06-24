package com.hy9be.spaperf.util;

import com.eclipsesource.json.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hyou on 6/23/15.
 */
public class RegExpWrapper {

    public static Matcher firstMatch(JsonObject regExp, String input) {
        // Reset multimatch regex state
        regExp.set("lastIndex", 0);
        Pattern r = Pattern.compile(regExp.get("").asString());
        return r.matcher(input);
    }

    public static JsonObject matcher(JsonObject regExp, String input) {
        // Reset regex state for the case
        // someone did not loop over all matches
        // last time.
        regExp.set("lastIndex", 0);
        return new JsonObject().add("re", regExp).add("input", input);
    }
}
