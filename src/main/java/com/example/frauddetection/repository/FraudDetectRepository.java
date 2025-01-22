package com.example.frauddetection.repository;

import com.example.frauddetection.Entity.FraudDetectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudDetectRepository extends JpaRepository<FraudDetectEntity, Integer> {

    FraudDetectEntity findFraudDetectEntityByTransactionId(String TransactionId);
}
