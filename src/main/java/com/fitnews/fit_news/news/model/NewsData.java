package com.fitnews.fit_news.news.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class NewsData {
    private String title;
    private String link;
    private String description;

    private Tc newsTc=null;
    private Boolean isClassified=false;

    public NewsData(String title, String link, String description){
        this.title=title;
        this.link=link;
        this.description=description;
    }

    @Override
    public String toString(){
        return title;
    }

}
