package com.example.frauddetection.repository;

import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FraudDetectRetryRepository extends JpaRepository<FraudDetectRetryEntity, Integer> {

    FraudDetectRetryEntity findFraudDetectRetryEntitiesByTransactionId(String TransactionId);

    List<FraudDetectRetryEntity> findFraudDetectRetryEntitiesByRetryStatEqualsAndRetryTimesLessThanAndUpdatedAtGreaterThan(
            Integer RetryStat, Integer retryTimes, Long updatedAt
    );
}
