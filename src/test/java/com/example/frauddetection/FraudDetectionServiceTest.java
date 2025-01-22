package com.example.frauddetection;

import com.example.frauddetection.Entity.FraudDetectEntity;
import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import com.example.frauddetection.Enum.RetryStatEnum;
import com.example.frauddetection.Enum.StatEnum;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.kafka.FraudDetectionProducer;
import com.example.frauddetection.repository.FraudDetectRepository;
import com.example.frauddetection.repository.FraudDetectRetryRepository;
import com.example.frauddetection.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FraudDetectionApplication.class)
public class FraudDetectionServiceTest {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private FraudDetectRepository repository;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
        retryRepository.deleteAll();
    }

    // 查询代码
    @Test
    public void testQueryFraudDetectResultByTransactionId() throws Exception {
        FraudDetectEntity entity = new FraudDetectEntity();
        entity.setTransactionId("transactionId301");
        entity.setUserId(111);
        entity.setAmount(100);
        entity.setBussTime(System.currentTimeMillis() - 10000);
        entity.setStat(StatEnum.PASS.getStat());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        repository.save(entity);

        int stat = fraudDetectionService.queryFraudDetectResultByTransactionId(entity.getTransactionId());
        Assertions.assertTrue(entity.getStat().equals(stat));
    }
}
