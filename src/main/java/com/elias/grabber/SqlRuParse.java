package com.elias.grabber;

import java.util.ArrayList;
import java.util.List;

import com.elias.grabber.utils.SqlRuDateTimeParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SqlRuParse {

    private static final String VACANCY_URL =
            "https://www.sql.ru/forum/1325330/lidy-be-fe-senior-cistemnye-analitiki-qa-i-devops-moskva-do-200t";

    public static void main(String[] args) throws Exception {
        List<String> pages = getPages();
        for (String url : pages) {
            Document doc = Jsoup.connect(url).get();
            Elements rows = doc.select(".postslisttopic");
            printAllVacancies(rows);
        }
        printVacancyDetail(VACANCY_URL);
    }

    private static void printAllVacancies(List<Element> row) {
        var dateParser = new SqlRuDateTimeParser();
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

    private static void printVacancyDetail(String url) throws Exception {
        Document doc = Jsoup.connect(url).get();
        String vacancyDescription = getVacancyDescription(doc);
        String date = getVacancyDate(doc);
        System.out.println("Описание: " + vacancyDescription);
        System.out.println("Дата: " + date);
    }

    private static String getVacancyDescription(Document document) {
        StringBuilder builder = new StringBuilder();
        String html = document.select(".msgBody").get(1).html();
        String[] htmlArr = html.split("<br>");
        for (String str : htmlArr) {
            builder.append(str).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static String getVacancyDate(Document doc) {
        return doc.select(".msgFooter").get(0).text().substring(0, 16);
    }

    private static List<String> getPages() {
        var url = "https://www.sql.ru/forum/job-offers/";
        var pages = new ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
            pages.add(url + i);
        }
        return pages;
    }

}