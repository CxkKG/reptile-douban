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
        login();
        while (!movieUrlQueue.isEmpty()) {
            String currentMovieUrl = movieUrlQueue.poll();
            long movieDataStartTime = System.currentTimeMillis();

            if (currentMovieUrl == null) {
                break;
            }

            processMoviePage(currentMovieUrl,movieDataStartTime);
        }

        System.out.println("爬取完成！");
    }

    private void processMoviePage(String movieUrl,long movieDataStartTime) {
        try {
            int count = 1;
            // 随机生成User-Agent头
            String randomUserAgent = getRandomUserAgent();

            Document moviePage = Jsoup.connect(movieUrl).userAgent(randomUserAgent).get();
            String movieName = moviePage.selectFirst("span[property=v:itemreviewed]").text();
            String movieRating = moviePage.selectFirst("strong[property=v:average]").text();

            if (!visitedMovies.contains(movieName)) {
                // 查询数据库，检查电影是否已经存在
                if(!isMovieExists(movieName)) {
                    // 将电影信息插入数据库
                    saveMovieInfo(movieName,movieRating);
                }
                long movieDataEndTime = System.currentTimeMillis();
                logger.info("电影爬取耗时:", movieDataEndTime - movieDataStartTime);
                System.out.println(movieDataEndTime - movieDataStartTime);
                //logger.info("电影名称：{}",movieName);
                //logger.info("电影评分：{}",movieRating);
                visitedMovies.add(movieName);

                //找到电影的演职员表
                long castStart = System.currentTimeMillis();
                Elements castLinks = moviePage.select("a[rel=v:starring], a[rel=v:directedBy]");
                List<Element> randomActors = getRandomElements(castLinks, 5);
                for (Element castLink : randomActors) {
                    String castUrl = castLink.attr("abs:href");
                   // System.out.println(castUrl);
                    if (!visitedCast.contains(castUrl)) {
                        castUrlQueue.add(castUrl);
                        visitedCast.add(castUrl);
                    }
                }
                long castEnd = System.currentTimeMillis();
                logger.info("演员爬取耗时:", castEnd - castStart);
                System.out.println(castEnd - castStart);
                //通过演员主页找到相关电影

                while (!castUrlQueue.isEmpty()) {
                    String cast = castUrlQueue.poll();
                    long castForMovieStart = System.currentTimeMillis();
                    Document cast1 = Jsoup.connect(cast).userAgent(randomUserAgent).get();
                    Elements castHomeLink = cast1.select("a[rel=v:starring], a[href*=subject]");
                    List<Element> randomMovies = getRandomElementsMovie(castHomeLink, 5);
                    for(Element movie : randomMovies) {
                        if (movie != null) {
                            String castHomePageUrl = movie.attr("abs:href");
                            //System.out.println(castHomePageUrl);
                            if (!visitedMovies.contains(castHomePageUrl)) {
                                movieUrlQueue.add(castHomePageUrl);
                                visitedMovies.add(castHomePageUrl);
                            }
                        }
                    }
                    long castForMovieEnd = System.currentTimeMillis();
                    logger.info("通过演员爬取电影耗时:",castForMovieEnd - castForMovieStart);
                    System.out.println(castForMovieEnd - castForMovieStart);
                    randomWait();
                }
                count++;
                randomWait();

                if(count == 10) {
                    longWait();
                    count -= 10;
                }

            }
        } catch (IOException e) {
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

    private void randomWait() {
        try {
            Random random = new Random();
            int waitTime = random.nextInt(3000) + 1000; // 随机生成 1000 到 4000 毫秒的等待时间
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void longWait() {
        try {
            Thread.sleep(60000); // 长时间等待，例如 60 秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<Element> getRandomElements(Elements elements, int count) {
        List<Element> randomElements = new ArrayList<>(count);
        List<Integer> indices = new ArrayList<>(elements.size());

        for (int i = 0; i < elements.size(); i++) {
            indices.add(i);
        }

        Random random = new Random();
        for (int i = 0; i < count; i++) {
            if (!indices.isEmpty()) {
                int randomIndex = random.nextInt(indices.size());
                int elementIndex = indices.get(randomIndex);
                randomElements.add(elements.get(elementIndex));
                indices.remove(randomIndex);
            }
        }

        return randomElements;
    }

    public static List<Element> getRandomElementsMovie(Elements elements, int count) {
        List<Element> randomElements = new ArrayList<>(count);
        List<Integer> indices = new ArrayList<>(elements.size());

        for (int i = 0; i < elements.size(); i++) {
            indices.add(i);
        }

        Random random = new Random();
        for (int i = 0; i < count; i++) {
            if (!indices.isEmpty()) {
                int randomIndex = random.nextInt(indices.size());
                int elementIndex = indices.get(randomIndex);
                randomElements.add(elements.get(elementIndex));
                indices.remove(randomIndex);
            }
        }

        return randomElements;
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