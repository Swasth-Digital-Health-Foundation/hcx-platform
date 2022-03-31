package org.swasth.apigateway.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@UtilityClass
public class Utils {

    private static final AntPathMatcher matcher = new AntPathMatcher();
    public static boolean containsRegexPath(List<String> regexPaths, String requestPath){
        for(String regexPath : regexPaths){
            if(matcher.isPattern(regexPath)){
                if(matcher.match(regexPath, requestPath))
                    return true;
            }
        }
        return false;
    }

    public static boolean isUUID(String s){
        return s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

}
