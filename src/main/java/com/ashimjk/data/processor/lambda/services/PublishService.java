package com.ashimjk.data.processor.lambda.services;

import com.ashimjk.data.processor.lambda.entities.Publish;
import com.ashimjk.data.processor.lambda.entities.Staging;
import com.ashimjk.data.processor.lambda.entities.Status;
import com.ashimjk.data.processor.lambda.models.PublishItem;
import com.ashimjk.data.processor.lambda.models.PublishResult;
import com.ashimjk.data.processor.lambda.models.StagingResult;
import com.ashimjk.data.processor.lambda.repositories.StagingRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublishService {

    private final EntityManager entityManager;
    private final StagingRepository stagingRepository;

    @Value("${ajk.data.processor.partition-size:5}")
    private int PARTITION_SIZE;

    public List<PublishItem> getPublishItems(List<StagingResult> stagingResults) {
        List<PublishItem> publishItems = new ArrayList<>();
        String transactionId = stagingResults.get(0).transactionId();

        Integer countStagings = stagingRepository.countByStatus(Status.PENDING.getValue());
        int partition = (int) Math.ceil((double) countStagings / PARTITION_SIZE);

        for (int i = 0; i < PARTITION_SIZE; i++) {
            int offset = i * partition;
            int limit = Math.min(partition, countStagings - offset);

            publishItems.add(new PublishItem(transactionId, offset, limit));
        }

        return publishItems;
    }

    @Transactional("oracleTransactionManager")
    public PublishResult execute(PublishItem publishItem) {
        List<Staging> stagings = stagingRepository.fetchByStatus(Status.PENDING.getValue(), publishItem.offset(), publishItem.limit());

        for (Staging staging : stagings) {
            Publish publish = new Publish();
            publish.setCustomerId(staging.getCustomerId());
            publish.setSurname(staging.getSurname());
            publish.setCScore(staging.getCScore());
            publish.setAge(staging.getAge());
            publish.setTenure(staging.getTenure());
            publish.setStatus(Status.COMPLETED.getValue());

            entityManager.merge(publish);
        }
        entityManager.flush();
        entityManager.clear();

        return new PublishResult(publishItem.transactionId(), publishItem.limit(), true);
    }

}
