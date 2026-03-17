package org.project.util;

public class StringUtil {

    public static boolean isBlank(String s){
        return s == null || s.isEmpty();
    }

    public static boolean isNotBlank(String s){
        if (s != null && s.length()>0){
            return true;
        }
        return false;
    }
}
