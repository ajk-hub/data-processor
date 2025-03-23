package com.ashimjk.data.processor.batch.syncup;

import com.ashimjk.data.processor.batch.entities.Customer;
import com.ashimjk.data.processor.common.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.PartitionStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.OraclePagingQueryProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ashimjk.data.processor.batch.syncup.SyncUpQuery.*;

@Configuration
public class SyncUpConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SyncUpConfig.class);

    @Value("${ajk.data.processor.partition-size:5}")
    private int PARTITION_SIZE;

    @Value("${ajk.data.processor.sync-up.chunk-size:5}")
    private int SYNC_UP_CHUNK_SIZE;

    @Bean
    @ConditionalOnProperty(prefix = "ajk.data.processor.sync-up.batch-job", name = "auto-start", havingValue = "true")
    public CommandLineRunner syncUpJobRunner(JobRunner jobRunner, Job syncUpJob) {
        return (args) -> jobRunner.runAndClose(syncUpJob);
    }

    @Bean
    public Job syncUpJob(JobRepository jobRepository, Step syncUpPartitionStep) {
        return new JobBuilder("sync-up-job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(syncUpPartitionStep)
                .build();
    }

    @Bean
    public Step syncUpPartitionStep(JobRepository jobRepository, Partitioner syncUpPartitioner, Step syncUpStep) {
        return new PartitionStepBuilder(new StepBuilder("sync-up-partition-step", jobRepository))
                .partitioner("syncUpStep", syncUpPartitioner)
                .step(syncUpStep)
                .gridSize(PARTITION_SIZE)
                .taskExecutor(syncUpTaskExecutor())
                .build();
    }

    @Bean
    public Partitioner syncUpPartitioner(@Qualifier("oracleDataSource") DataSource dataSource) {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();

            int totalCustomers = getTotalCustomers(dataSource);
            int customerPerPartition = (int) Math.ceil((double) totalCustomers / gridSize);

            LOG.info("Grid Size : {}", gridSize);
            LOG.info("Total Customers : {}", totalCustomers);
            LOG.info("Customer Per Partition : {}", customerPerPartition);

            for (int i = 0; i < gridSize; i++) {
                int offset = i * customerPerPartition;
                int limit = Math.min(customerPerPartition, totalCustomers - offset);

                ExecutionContext context = new ExecutionContext();
                context.putInt("offset", offset);
                context.putInt("limit", limit);
                context.putInt("partitionIndex", i);

                partitions.put("partition" + i, context);
            }

            return partitions;
        };
    }

    @Bean
    public Step syncUpStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Customer> syncUpReader,
            ItemWriter<Customer> syncUpWriter
    ) {
        return new StepBuilder("sync-up-step", jobRepository)
                .<Customer, Customer>chunk(SYNC_UP_CHUNK_SIZE, transactionManager)
                .reader(syncUpReader)
                .writer(syncUpWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> syncUpReader(@Qualifier("oracleDataSource") DataSource dataSource,
                                                       @Value("#{stepExecutionContext['offset']}") Integer offset,
                                                       @Value("#{stepExecutionContext['limit']}") Integer limit,
                                                       @Value("#{stepExecutionContext['partitionIndex']}") Integer partitionIndex) {

        LOG.info("Initializing Reader --> Partition {} => offset: {}, limit: {}", partitionIndex, offset, limit);

        OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
        queryProvider.setSelectClause(SYNC_UP_SELECT_QUERY);
        queryProvider.setFromClause(SYNC_UP_FROM_QUERY);
        queryProvider.setSortKeys(Collections.singletonMap("CUSTOMER_ID", Order.ASCENDING));

        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(SYNC_UP_CHUNK_SIZE);
        reader.setRowMapper(new BeanPropertyRowMapper<>(Customer.class));
        reader.setQueryProvider(queryProvider);
        reader.setParameterValues(Map.of(
                "offset", offset,
                "limit", limit
        ));

        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Customer> syncUpWriter(NamedParameterJdbcTemplate jdbcTemplate) {
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();
        writer.setJdbcTemplate(jdbcTemplate);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO customer_syncup (customer_id, surname, c_score, age, tenure, status) " +
                "VALUES (:customerId, :surname, :cScore, :age, :tenure, 'NW')");
        return writer;
    }

    @Bean
    public TaskExecutor syncUpTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("PartitionThread-");
        executor.initialize();
        return executor;
    }

    private Integer getTotalCustomers(DataSource dataSource) {
        return new JdbcTemplate(dataSource)
                .queryForObject(SYNC_UP_COUNT_QUERY, Integer.class);
    }

}
