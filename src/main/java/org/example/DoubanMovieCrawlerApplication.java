package org.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DoubanMovieCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoubanMovieCrawlerApplication.class, args);
    }

}
