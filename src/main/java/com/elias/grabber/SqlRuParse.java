package com.elias.grabber;

import java.util.ArrayList;
import java.util.List;

import com.elias.grabber.model.Post;
import com.elias.grabber.utils.DateTimeParser;
import com.elias.grabber.utils.SqlRuDateTimeParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRuParse {

    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class.getName());

    private static final String VACANCY_URL = "https://www.sql.ru/forum/1334377/razrabotchik-pl-sql?hl=pl%20sql";

    public static void main(String[] args) {
        try {
            DateTimeParser dateParser = new SqlRuDateTimeParser();
            List<String> pages = getPages();
            for (String url : pages) {
                Document doc = Jsoup.connect(url).get();
                Elements rows = doc.select(".postslisttopic");
                printAllVacancies(rows, dateParser);
            }
            System.out.println(getPost(VACANCY_URL, dateParser));
        } catch (Exception e) {
            LOG.error("Parse site error", e);
        }
    }

    private static void printAllVacancies(List<Element> row, DateTimeParser dateParser) {
        var vacancies = row.subList(3, row.size());
        for (Element td : vacancies) {
            Element parent = td.parent();
            var job = parent.children().get(1).child(0);
            var jobText = job.text();
            var jobHref = job.attr("href");
            var author = parent.children().get(2).child(0).text();
            var date = dateParser.parse(parent.children().get(5).text());
            System.out.println("Вакансия: " + jobText);
            System.out.println("Дата: " + date);
            System.out.println("Автор: " + author);
            System.out.println(jobHref);
        }
    }

    private static List<String> getPages() {
        var url = "https://www.sql.ru/forum/job-offers/";
        var pages = new ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
            pages.add(url + i);
        }
        return pages;
    }

    private static Post getPost(String link, DateTimeParser dateParser) throws Exception {
        var doc = Jsoup.connect(link).get();
        var title = getVacancyTitle(doc);
        var description = getVacancyDescription(doc);
        var createdDate = dateParser.parse(getVacancyDate(doc));
        return new Post(title, link, description, createdDate);
    }

    private static String getVacancyTitle(Document doc) {
        return doc.select(".messageHeader").first().ownText();
    }

    private static String getVacancyDescription(Document document) {
        return document.select(".msgBody").get(1).text();
    }

    private static String getVacancyDate(Document doc) {
        return doc.select(".msgFooter").first().text().substring(0, 16);
    }

}