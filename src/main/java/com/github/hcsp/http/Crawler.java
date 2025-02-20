package com.github.hcsp.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Crawler {
    static class GitHubPullRequest {
        // Pull request的编号
        int number;
        // Pull request的标题
        String title;
        // Pull request的作者的GitHub id
        String author;

        GitHubPullRequest(int number, String title, String author) {
            this.number = number;
            this.title = title;
            this.author = author;
        }
    }

    // 给定一个仓库名，例如"golang/go"，或者"gradle/gradle"，返回第一页的Pull request信息
    public static List<GitHubPullRequest> getFirstPageOfPullRequests(String repo) throws IOException {

        List<GitHubPullRequest> list = new ArrayList<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://github.com/" + repo + "/pulls");
        CloseableHttpResponse response = httpclient.execute(httpGet);

        // Please note that if response content is not fully consumed the underlying

        try {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            String result = IOUtils.toString(inputStream, "UTF-8");

            Document doc = Jsoup.parse(result);
            Elements issues = doc.select(".js-issue-row");
            for (Element element : issues) {
                String title = element.select(".lh-condensed").select("a").get(0).text();
                String[] issueMsg = element.select(".opened-by").get(0).text().split(" ");
                String number = issueMsg[0].substring(1);
                String author = issueMsg[issueMsg.length - 1];
                list.add(new GitHubPullRequest(Integer.parseInt(number), title, author));

            }
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        return list;
    }

    public static void main(String[] args) {
        try {
            getFirstPageOfPullRequests("gradle/gradle");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
