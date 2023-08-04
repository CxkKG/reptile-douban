package org.example.web;

import jakarta.transaction.Transactional;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class DoubanMovieCrawler {
    private final MovieRepository movieRepository;
    @Autowired
    public DoubanMovieCrawler(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public final Queue<String> movieUrlQueue = new LinkedList<>();
    private final Queue<String> castUrlQueue = new LinkedList<>();
    private final Set<String> visitedMovies = new HashSet<>();
    private final Set<String> visitedCast = new HashSet<>();
    private final String username = "15725153249";
    private final String password = "caisinimabi.0616";
    private static final Logger logger = LoggerFactory.getLogger(DoubanMovieCrawler.class);

    @PostConstruct
    public void recursiveCrawl() {
        movieUrlQueue.add("https://movie.douban.com/subject/1292052/");
        while (!movieUrlQueue.isEmpty()) {
            String currentMovieUrl = movieUrlQueue.poll();

            if (currentMovieUrl == null) {
                break;
            }

            processMoviePage(currentMovieUrl);
        }

        System.out.println("爬取完成！");
    }

    private void processMoviePage(String movieUrl) {
        try {
            // 随机生成User-Agent头
            String randomUserAgent = getRandomUserAgent();

            Document moviePage = Jsoup.connect(movieUrl).userAgent(randomUserAgent).get();
            String movieName = moviePage.selectFirst("span[property=v:itemreviewed]").text();
            String movieRating = moviePage.selectFirst("strong[property=v:average]").text();

            if (!visitedMovies.contains(movieName)) {
                // 这里可以写入数据库的逻辑，此处省略
                // 查询数据库，检查电影是否已经存在
                if(!isMovieExists(movieName)) {
                    // 将电影信息插入数据库
                    saveMovieInfo(movieName,movieRating);
                }
                logger.info("电影名称：{}",movieName);
                logger.info("电影评分：{}",movieRating);
                visitedMovies.add(movieName);

                //找到电影的演职员表
                Elements castLinks = moviePage.select("a[rel=v:starring], a[rel=v:directedBy]");
                for (Element castLink : castLinks) {
                    String castUrl = castLink.attr("abs:href");
                   // System.out.println(castUrl);
                    if (!visitedCast.contains(castUrl)) {
                        castUrlQueue.add(castUrl);
                        visitedCast.add(castUrl);
                    }
                }
                //通过演员主页找到相关电影
                while (!castUrlQueue.isEmpty()) {
                    String cast = castUrlQueue.poll();
                    Document cast1 = Jsoup.connect(cast).userAgent(randomUserAgent).get();
                    Elements castHomeLink = cast1.select("a[rel=v:starring], a[href*=subject]");
                    for(Element movie : castHomeLink) {
                        if (movie != null) {
                            String castHomePageUrl = movie.attr("abs:href");
                            //System.out.println(castHomePageUrl);
                            if (!visitedMovies.contains(castHomePageUrl)) {
                                movieUrlQueue.add(castHomePageUrl);
                                visitedMovies.add(castHomePageUrl);
                            }
                        }
                    }
                }
                Thread.sleep(3000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void login() {
        try {
            Connection.Response loginForm = Jsoup.connect("https://accounts.douban.com/login")
                    .method(Connection.Method.GET)
                    .execute();

            Document loginPage = loginForm.parse();
            String captchaId = loginPage.select("input[name=captcha-id]").val();
            String captchaUrl = loginPage.select("img#captcha_image").attr("src");

            // 如果有验证码，可以在此处处理验证码识别

            Map<String, String> loginData = new HashMap<>();
            loginData.put("form_email", username);
            loginData.put("form_password", password);
            loginData.put("captcha-solution", ""); // 如果有验证码，需要填写验证码的值
            loginData.put("captcha-id", captchaId);

            Connection.Response loginResponse = Jsoup.connect("https://accounts.douban.com/login")
                    .data(loginData)
                    .method(Connection.Method.POST)
                    .execute();

            // 登录成功后，获取登录后的Cookie，并在后续的请求中添加到请求头中
            Map<String, String> cookies = loginResponse.cookies();
            // 将Cookie保存起来，用于后续的请求
            // ...

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomUserAgent() {
        String[] commonUserAgents = {
                // 常见的User-Agent头
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15"
                // 可根据需求添加更多User-Agent头
        };

        Random random = new Random();
        int randomIndex = random.nextInt(commonUserAgents.length);
        return commonUserAgents[randomIndex];
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