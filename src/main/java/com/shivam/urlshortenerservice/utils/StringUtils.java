package com.shivam.urlshortenerservice.utils;

import java.util.Set;

public class StringUtils {
    public static boolean isEmpty(String str){
        return str == null || str.isBlank();
    }

    public static boolean isEmpty(Set<String> set){
        return set == null || set.isEmpty();
    }
}
