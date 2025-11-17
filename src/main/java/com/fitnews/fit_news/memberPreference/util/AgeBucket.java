package com.fitnews.fit_news.memberPreference.util;

public class AgeBucket {

    public static int toBucket(char age) {
        // a=0, b=1, c=2, d=3
        return switch (age) {
            case 'a' -> 0;
            case 'b' -> 1;
            case 'c' -> 2;
            case 'd' -> 3;
            default  -> 1; // 기본 청년
        };
    }
}
