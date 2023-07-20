package org.example;

import org.example.web.DoubanMovieCrawlerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EntityScan(basePackages = "org.example.web")
public class DoubanMovieCrawlerApplication {
    private final DoubanMovieCrawlerService crawlerService;

    public DoubanMovieCrawlerApplication(DoubanMovieCrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DoubanMovieCrawlerApplication.class, args);
    }

    @PostConstruct
    public void startCrawling() {
        crawlerService.crawlAndSaveMovies();
    }
}