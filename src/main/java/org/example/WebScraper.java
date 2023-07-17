package org.example;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class WebScraper {
    public static void main(String[] args) {
        // 发送HTTP请求获取网页内容
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://www.douban.com/");
            httpGet.setHeader("User-Agent","Mozilla/5.0_ (Windows_ NT. 10.0; Win64; x64) AppleWebKit/537.36 (KHTML， like Gecko) Chrome/88.0.4324.104 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String html = EntityUtils.toString(response.getEntity());

            // 使用Jsoup解析HTML
            Document document = Jsoup.parse(html);
            document.outputSettings().charset("UTF-8");
            // 提取网页标题
            String pageTitle = document.title();
            System.out.println("网页标题: " + pageTitle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}