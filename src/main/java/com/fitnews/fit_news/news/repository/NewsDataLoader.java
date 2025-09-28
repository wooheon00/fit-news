package com.fitnews.fit_news.news.repository;


import com.fitnews.fit_news.news.entity.News;
import com.fitnews.fit_news.news.util.OgImageFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NewsDataLoader implements CommandLineRunner {

    private final NewsRepository newsRepository;

    @Override
    public void run(String... args) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.ENGLISH);

        saveIfNotExists(
                "이재명 대통령 \"국정 책임자로서 송구\"...\"수수료 한시 면제 방법 찾아달라\" 지시",
                "https://news.jtbc.co.kr/article/NB12264844",
                "대전 국가정보자원관리원 화재로 인한 국가 전산망 마비 사태와 관련해 이재명 대통령이 \"국정 최고 책임자로서 송구하다\"며 공식 사과했습니다.",
                LocalDateTime.parse("Sun, 28 Sep 2025 22:32:43 +0900", formatter)
        );

        saveIfNotExists(
                "한·중간 지방정부 교류도 활기…첨단산업 협력 방점",
                "https://news.jtbc.co.kr/article/NB12264808",
                "이재명 정부가 국익 중심의 실용 외교를 하겠다고 선언했는데요. 한·중 관계 개선에 맞춰서 지방정부 차원의 교류 협력도 다시 활기를 띠고 있습니다.",
                LocalDateTime.parse("Sun, 28 Sep 2025 19:37:23 +0900", formatter)
        );

        saveIfNotExists(
                "민주 '조희대 청문회' 압박 공세…국힘 '6년만' 서울 장외 집회",
                "https://news.jtbc.co.kr/article/NB12264827",
                "민주당은 지난주 검찰개혁 입법을 마무리한 데 이어서 모레에는 조희대 대법원장 청문회를 엽니다. 국민의힘은 서울에서 장외집회를 열었습니다.",
                LocalDateTime.parse("Sun, 28 Sep 2025 19:33:30 +0900", formatter)
        );
    }

    private void saveIfNotExists(String title, String link, String description, LocalDateTime pubDate) {
        newsRepository.findByLink(link).orElseGet(() -> {
            News news = new News();
            news.setTitle(title);
            news.setLink(link);
            news.setDescription(description);
            news.setPubDate(pubDate);

            // ✅ 썸네일 자동 추출
            String ogImage = OgImageFetcher.getOgImage(link);
            if (ogImage != null) {
                news.setThumbnailUrl(ogImage);
            } else {
                news.setThumbnailUrl("/images/default-thumb.png");
            }

            return newsRepository.save(news);
        });
    }
}