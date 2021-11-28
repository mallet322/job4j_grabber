package com.elias.grabber.utils;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRuDateTimeParser implements DateTimeParser {

    private static final Logger LOG = LoggerFactory.getLogger(SqlRuDateTimeParser.class.getName());
    private static final String[] MONTHS = new String[] {
            "янв", "фев", "мар", "апр", "май", "июнь", "июль", "авг", "сен", "окт", "ноя", "дек"
    };
    private static final Locale LOCALE_RU = new Locale("ru");
    private static final String DATE_FORMAT_PATTERN = "dd MMM yy, HH:mm";
    private static final String TODAY = "сегодня";
    private static final String YESTERDAY = "вчера";

    @Override
    public LocalDateTime parse(String parse) {
        var dateFormatSymbols = getCustomMonths();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, dateFormatSymbols);
        return parse(parse, simpleDateFormat);
    }

    private LocalDateTime parse(String parse, SimpleDateFormat format) {
        Date date = new Date();
        try {
            var dates = parse.split(", ");
            if (TODAY.equals(dates[0])) {
                date = asDate(LocalDate.now(), LocalTime.parse(dates[1]));
            } else if (YESTERDAY.equals(dates[0])) {
                date = asDate(LocalDate.now().minusDays(1), LocalTime.parse(dates[1]));
            } else {
                date = format.parse(parse);
            }
        } catch (ParseException e) {
            LOG.error("DateTimeParser", e);
        }
        return asLocalDateTime(date);
    }

    private static Date asDate(LocalDate localDate, LocalTime localTime) {
        Instant instant = localTime.atDate(localDate).
                                   atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    private static LocalDateTime asLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static DateFormatSymbols getCustomMonths() {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(LOCALE_RU);
        dateFormatSymbols.setMonths(MONTHS);
        return dateFormatSymbols;
    }

}
