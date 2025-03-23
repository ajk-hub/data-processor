package com.ashimjk.data.processor.configs;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.ashimjk.data.processor.lambda.repositories",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef = "oracleTransactionManager"
)
public class DataSourceConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "ajk.data.processor.datasource.h2", name = "enabled", havingValue = "true")
    public static class H2DataSourceConfig {

        @Bean
        @ConfigurationProperties("spring.datasource")
        public DataSourceProperties h2DataSourceProperties() {
            return new DataSourceProperties();
        }

        @Primary
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public DataSource h2DataSource() {
            return h2DataSourceProperties()
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();
        }

        @Bean
        @Primary
        public DataSourceTransactionManager h2TransactionManager() {
            return new DataSourceTransactionManager(h2DataSource());
        }
    }

    @Bean
    @ConfigurationProperties("spring.datasource.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.oracle.hikari")
    public DataSource oracleDataSource() {
        return oracleDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(oracleDataSource())
                .packages("com.ashimjk.data.processor.lambda.entities")
                .persistenceUnit("oracle")
                .build();
    }

    @Bean
    public PlatformTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
