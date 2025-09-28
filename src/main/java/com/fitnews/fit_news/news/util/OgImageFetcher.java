package com.fitnews.fit_news.news.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class OgImageFetcher {

    public static String getOgImage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0") // 일부 사이트는 User-Agent 필요
                    .timeout(5000)
                    .get();

            Element meta = doc.selectFirst("meta[property=og:image]");
            if (meta != null) {
                return meta.attr("content");
            }
        } catch (Exception e) {
            System.err.println("[OgImageFetcher] 썸네일 추출 실패: " + url);
        }
        return null;
    }
}
