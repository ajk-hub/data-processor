package com.ashimjk.data.processor.lambda.repositories;

import com.ashimjk.data.processor.lambda.entities.SyncUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncUpRepository extends JpaRepository<SyncUp, Long> {

    @Query(value = "SELECT * FROM CUSTOMER_SYNCUP WHERE STATUS = :status ORDER BY CUSTOMER_ID OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",
            nativeQuery = true)
    List<SyncUp> fetchByStatus(@Param("status") String status, @Param("offset") long offset, @Param("limit") long limit);

    Integer countByStatus(String status);

}
