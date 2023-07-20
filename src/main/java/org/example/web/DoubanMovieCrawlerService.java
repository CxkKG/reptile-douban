package org.example.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DoubanMovieCrawlerService {
    private final MovieRepository movieRepository;
    @Lazy
    @Autowired
    public DoubanMovieCrawlerService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void crawlAndSaveMovies() {
        int page = 0;
        int pageSize = 25;
        int pageCount = 10;
        try {
            for (int i = 0; i < pageCount; i++) {
                String url = "https://movie.douban.com/top250?start=" + (page * pageSize);
                // 建立连接并获取HTML内容
                Document doc = Jsoup.connect(url).get();
                // 解析HTML内容，提取评分信息
                Elements movieElements = doc.select("div.item");

                for (Element movieElement : movieElements) {
                    Element ratingElement = movieElement.selectFirst("div.bd div.star span.rating_num");
                    String rating = ratingElement.text();

                    Element titleElement = movieElement.selectFirst("div.hd a span.title");
                    String title = titleElement.text();

                   Movie newMovie = new Movie();
                   newMovie.setTitle(title);
                   newMovie.setRating(rating);

                   movieRepository.save(newMovie);
                }
                page++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}