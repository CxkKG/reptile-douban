package org.example;

import org.example.web.DoubanMovieCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties
public class DoubanMovieCrawlerApplication {

    @Autowired
    private DoubanMovieCrawler doubanMovieCrawler;

    public static void main(String[] args) {
        SpringApplication.run(DoubanMovieCrawlerApplication.class, args);
    }

    // 在应用程序启动时执行爬虫
    @PostConstruct
    public void startCrawling() {
        String actorPageUrl = "https://movie.douban.com/celebrity/1054396/movies";
        DoubanMovieCrawler crawler = new DoubanMovieCrawler();
        crawler.crawlActorPage(actorPageUrl);
    }
}
