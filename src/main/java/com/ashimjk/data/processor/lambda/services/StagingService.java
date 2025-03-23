package com.ashimjk.data.processor.lambda.services;

import com.ashimjk.data.processor.lambda.entities.Staging;
import com.ashimjk.data.processor.lambda.entities.Status;
import com.ashimjk.data.processor.lambda.entities.SyncUp;
import com.ashimjk.data.processor.lambda.models.StagingItem;
import com.ashimjk.data.processor.lambda.models.StagingResult;
import com.ashimjk.data.processor.lambda.models.SyncUpResult;
import com.ashimjk.data.processor.lambda.repositories.SyncUpRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StagingService {

    private final EntityManager entityManager;
    private final SyncUpRepository syncUpRepository;

    @Value("${ajk.data.processor.partition-size:5}")
    private int PARTITION_SIZE;

    public List<StagingItem> getStagingItems(SyncUpResult syncUpResult) {
        Integer countStagings = syncUpRepository.countByStatus(Status.NEW.getValue());
        int partition = (int) Math.ceil((double) countStagings / PARTITION_SIZE);
        List<StagingItem> stagingItems = new ArrayList<>();

        for (int i = 0; i < PARTITION_SIZE; i++) {
            int offset = i * partition;
            int limit = Math.min(partition, countStagings - offset);

            stagingItems.add(new StagingItem(syncUpResult.transactionId(), offset, limit));
        }

        return stagingItems;
    }

    @Transactional("oracleTransactionManager")
    public StagingResult execute(StagingItem stagingItem) {
        List<SyncUp> syncUps = syncUpRepository.fetchByStatus(Status.NEW.getValue(), stagingItem.offset(), stagingItem.limit());

        for (SyncUp syncUp : syncUps) {
            Staging staging = new Staging();
            staging.setCustomerId(syncUp.getCustomerId());
            staging.setSurname(syncUp.getSurname());
            staging.setCScore(syncUp.getCScore());
            staging.setAge(syncUp.getAge());
            staging.setTenure(syncUp.getTenure());
            staging.setStatus(Status.PENDING.getValue());

            entityManager.merge(staging);
        }
        entityManager.flush();
        entityManager.clear();

        return new StagingResult(stagingItem.transactionId(), stagingItem.limit(), true);
    }

}
