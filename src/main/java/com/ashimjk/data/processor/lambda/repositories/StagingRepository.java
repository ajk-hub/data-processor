package com.ashimjk.data.processor.lambda.repositories;

import com.ashimjk.data.processor.lambda.entities.Staging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StagingRepository extends JpaRepository<Staging, Long> {

    @Query(value = "SELECT * FROM CUSTOMER_STAGING WHERE STATUS = :status ORDER BY CUSTOMER_ID OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",
            nativeQuery = true)
    List<Staging> fetchByStatus(@Param("status") String status, @Param("offset") long offset, @Param("limit") long limit);

    Integer countByStatus(String status);

}
