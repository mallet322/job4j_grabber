package com.elias.quartz;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SqlRuParse {

    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select(".postslisttopic");
        List<Element> jobs = row.subList(3, row.size());
        for (Element td : jobs) {
            Element parent = td.parent();
            var job = parent.children().get(1).child(0);
            var jobText = job.text();
            var jobHref = job.attr("href");
            var author = parent.children().get(2).child(0).text();
            var date = parent.children().get(5).text();
            System.out.println("Вакансия: " + jobText);
            System.out.println("Дата: " + date);
            System.out.println("Автор: " + author);
            System.out.println(jobHref);
        }
    }

}