package com.ashimjk.data.processor.common;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobRunner {

    private static final Logger LOG = LoggerFactory.getLogger(JobRunner.class);

    private final JobLauncher jobLauncher;
    private final List<DataSource> dataSources;
    private final List<TaskExecutor> taskExecutors;
    private final ConfigurableApplicationContext context;

    public void run(Job job) {
        try {
            jobLauncher.run(job, getJobParameters());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void runAndClose(Job job) {
        try {
            LOG.info("Starting batch job...");
            JobExecution jobExecution = jobLauncher.run(job, getJobParameters());
            LOG.info("Batch job completed. Shutting down...");

            shutdownTaskExecutor();
            shutdownDataSource();
            shutdownContext();

            int exitCode = jobExecution.getStatus() == BatchStatus.COMPLETED ? 0 : 1;
            System.exit(exitCode);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private JobParameters getJobParameters() {
        return new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
    }

    private void shutdownTaskExecutor() {
        taskExecutors.forEach(taskExecutor -> {
            if (taskExecutor instanceof ThreadPoolTaskExecutor) {
                ((ThreadPoolTaskExecutor) taskExecutor).shutdown();
            }
        });
    }

    private void shutdownDataSource() {
        dataSources.forEach(dataSource -> {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        });
    }

    private void shutdownContext() {
        context.close();
    }
}
