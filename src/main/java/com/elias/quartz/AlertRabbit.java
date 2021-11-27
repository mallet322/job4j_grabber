package com.elias.quartz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertRabbit {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class.getName());

    public static void main(String[] args) {
        var properties = getProperties();
        try (Connection connection = getConnection(properties)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = JobBuilder.newJob(Rabbit.class)
                                      .usingJobData(data)
                                      .build();
            SimpleScheduleBuilder times = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            LOG.error("Job error - ", se);
        }
    }

    private static Connection getConnection(Properties properties) throws Exception {
        Class.forName(properties.getProperty("driver"));
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            insertIntoTable(cn);
        }

        private static void insertIntoTable(Connection connection) {
            LOG.info("insert into rabbit table...");
            try (PreparedStatement statement =
                         connection.prepareStatement("insert into rabbit(created_date) values(?)")) {
                statement.setTimestamp(1, Timestamp.from(Instant.now()));
                statement.execute();
            } catch (Exception e) {
                LOG.error("Query error - ", e);
            }
        }

    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        File file = new File("src/main/resources/rabbit.properties");
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            LOG.error("Read settings error - ", e);
        }
        return properties;
    }

}