package com.oss.apigateway.utils;

import org.springframework.util.AntPathMatcher;

import java.util.List;

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

}
