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

    // 0. 将第一部电影放入队列，执行递归方法；
    // 1. 判断队列是否为空，如果为空则代码结束
    // 2. 找到电影的评分&名称
    // 2.1 先用电影名查数据库，看是否已经存在，已经存在则不写入数据库；不存在，则写入数据库；
    // 3. 找到电影的演职员表（包括导演&演员等），通过演职员表的人进入演职员表主页，找到相关电影，将相关电影链接找到，放入队列；
    // 4. 结束方法
    // 123

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