package com.ashimjk.data.processor.batch.initialize;

import com.ashimjk.data.processor.batch.entities.Customer;
import com.ashimjk.data.processor.common.JobRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class InitConfig {

    @Bean
    @ConditionalOnProperty(prefix = "ajk.data.processor.init.batch-job", name = "auto-start", havingValue = "true")
    public CommandLineRunner initJobRunner(JobRunner jobRunner, Job initJob) {
        return (args) -> jobRunner.runAndClose(initJob);
    }

    @Bean
    public Job initJob(Step initStep, JobRepository jobRepository) {
        return new JobBuilder("init-job", jobRepository)
                .start(initStep)
                .build();
    }

    @Bean
    public Step initStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemWriter<Customer> initWriter
    ) {
        return new StepBuilder("init-step", jobRepository)
                .<Customer, Customer>chunk(1000, transactionManager)
                .taskExecutor(initTaskExecutor())
                .reader(initReader())
                .processor(initProcessor())
                .writer(initWriter)
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> initReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("customers.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper(createLineMapper());
        return reader;
    }

    @Bean
    public ItemProcessor<Customer, Customer> initProcessor() {
        return customer -> customer;
    }

    @Bean
    public JdbcBatchItemWriter<Customer> initWriter(NamedParameterJdbcTemplate jdbcTemplate) {
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();
        writer.setJdbcTemplate(jdbcTemplate);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO customers (customer_id, surname, c_score, country, gender, age, tenure, balance, has_credit_card, is_active, estimated_salary) " +
                "VALUES (:customerId, :surname, :cScore, :country, :gender, :age, :tenure, :balance, :hasCreditCard, :isActive, :estimatedSalary)");
        return writer;
    }

    @Bean
    public TaskExecutor initTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("PartitionThread-");
        executor.initialize();
        return executor;
    }

    private static LineMapper<Customer> createLineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("customerId", "surname", "cScore", "country", "gender", "age", "tenure", "balance", "hasCreditCard", "isActive", "estimatedSalary");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

}
