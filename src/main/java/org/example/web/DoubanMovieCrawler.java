package org.example.web;

import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.*;

@Component
public class DoubanMovieCrawler {
    private MovieRepository movieRepository;

    public DoubanMovieCrawler() {
        this.movieRepository = movieRepository;
    }

    private final Set<String> visitedActors = new HashSet<>();
    private final Set<String> visitedMovies = new HashSet<>();
    private final Queue<String> actorUrlQueue = new LinkedList<>();
    private final Queue<String> movieUrlQueue = new LinkedList<>();

    public void crawlActorPage(String actorPageUrl) {
        actorUrlQueue.add(actorPageUrl);
        bfsCrawl();
    }

    private void bfsCrawl() {
        while (!actorUrlQueue.isEmpty()) {
            String currentActorUrl = actorUrlQueue.poll();
            if (!visitedActors.contains(currentActorUrl)) {
                List<String> movieUrls = getMoviesFromActorPage(currentActorUrl);
                visitedActors.add(currentActorUrl);

                for (String movieUrl : movieUrls) {
                    if (!visitedMovies.contains(movieUrl)) {
                        movieUrlQueue.add(movieUrl);
                        visitedMovies.add(movieUrl);
                    }
                }
            }
        }

        processMovieUrls();
    }

    private List<String> getMoviesFromActorPage(String actorPageUrl) {
        List<String> movieUrls = new ArrayList<>();
        try {
            Document actorPage = Jsoup.connect(actorPageUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36").get();
            Elements movieLinks = actorPage.select("div.info a[href^=/subject/]");

            for (Element link : movieLinks) {
                String relativeUrl = link.attr("href");
                String absoluteUrl = "https://movie.douban.com" + relativeUrl;
                movieUrls.add(absoluteUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movieUrls;
    }

    private void processMovieUrls() {
        while (!movieUrlQueue.isEmpty()) {
            String movieUrl = movieUrlQueue.poll();
            try {
                Document document = Jsoup.connect(movieUrl).get();
                String movieName = document.select("span[property=v:itemreviewed]").text();
                String movieScore = document.select("strong[property=v:average]").text();

                // 查询数据库，检查电影是否已经存在
                if (!isMovieExists(movieName)) {
                    // 将电影信息插入数据库
                    saveMovieInfo(movieName, movieScore);
                }

                System.out.println("电影链接：" + movieUrl);

                List<String> actorUrls = getActorsFromMoviePage(movieUrl);

                for (String actorUrl : actorUrls) {
                    if (!visitedActors.contains(actorUrl)) {
                        actorUrlQueue.add(actorUrl);
                        visitedActors.add(actorUrl);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getActorsFromMoviePage(String moviePageUrl) {
        List<String> actorUrls = new ArrayList<>();
        try {
            Document moviePage = Jsoup.connect(moviePageUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36").get();
            Elements actorLinks = moviePage.select("a[rel=v:starring]");

            for (Element link : actorLinks) {
                String actorUrl = link.attr("href");
                actorUrls.add(actorUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return actorUrls;
    }
        @Transactional
        private boolean isMovieExists(String movieName) {
            // 使用 JPA 查询数据库，检查电影是否已经存在
            return movieRepository.findByName(movieName) != null;
        }
        @Transactional
        private void saveMovieInfo(String movieName, String movieScore) {
            // 使用 JPA 将电影信息插入数据库
            Movie movie = new Movie();
            movie.setName(movieName);
            movie.setScore(movieScore);
            movieRepository.save(movie);
            System.out.println("电影：" + movieName + "，评分：" + movieScore + "，已写入数据库。");
        }
}