package com.ashimjk.data.processor.lambda;

import com.ashimjk.data.processor.common.JobRunner;
import com.ashimjk.data.processor.lambda.models.*;
import com.ashimjk.data.processor.lambda.services.PublishService;
import com.ashimjk.data.processor.lambda.services.StagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class LambdaHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LambdaHandler.class);

    @Bean
    public Supplier<InitResult> initLambda(JobRunner jobRunner, Job initJob) {
        return () -> {
            String id = UUID.randomUUID().toString();
            LOG.info("Starting init job with id: {}", id);

            jobRunner.run(initJob);

            LOG.info("Finished init job with id: {}", id);
            return new InitResult(id);
        };
    }

    @Bean
    public Function<InitResult, SyncUpResult> syncUpLambda(JobRunner jobRunner, Job syncUpJob) {
        return initResult -> {
            String id = initResult.transactionId();
            LOG.info("Starting sync-up job with id: {}", id);

            jobRunner.run(syncUpJob);

            LOG.info("Finished sync-up job with id: {}", id);
            return new SyncUpResult(id);
        };
    }

    @Bean
    public Function<SyncUpResult, Map<String, List<StagingItem>>> preStagingLambda(StagingService stagingService) {
        return syncUpResult -> {
            LOG.info("Processing pre-staging with id: {}", syncUpResult.transactionId());
            List<StagingItem> stagingItems = stagingService.getStagingItems(syncUpResult);
            return Map.of("stagingItems", stagingItems);
        };
    }

    @Bean
    public Function<StagingItem, StagingResult> stagingLambda(StagingService stagingService) {
        return stagingItem -> {
            LOG.info("Starting staging with id: {}, offset: {} and limit: {}", stagingItem.transactionId(), stagingItem.offset(), stagingItem.limit());
            StagingResult stagingResult = stagingService.execute(stagingItem);
            LOG.info("Finished staging with id: {}", stagingItem.transactionId());

            return stagingResult;
        };
    }

    @Bean
    public Function<List<StagingResult>, Map<String, List<PublishItem>>> prePublishLambda(PublishService publishService) {
        return stagingResults -> {
            String transactionId = stagingResults.get(0).transactionId();
            LOG.info("Processing pre-publish with id: {}", transactionId);

            List<PublishItem> publishItems = publishService.getPublishItems(stagingResults);
            return Map.of("publishItems", publishItems);
        };
    }

    @Bean
    public Function<PublishItem, PublishResult> publishLambda(PublishService publishService) {
        return publishItem -> {
            LOG.info("Starting publish with id: {}, offset: {} and limit: {}", publishItem.transactionId(), publishItem.offset(), publishItem.limit());
            PublishResult publishResult = publishService.execute(publishItem);
            LOG.info("Finished publish with id: {}", publishItem.transactionId());

            return publishResult;
        };
    }

    public record InitResult(String transactionId) {
    }

}
