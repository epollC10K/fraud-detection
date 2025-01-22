package com.example.frauddetection;

import com.alibaba.fastjson.JSONObject;
import com.example.frauddetection.Entity.FraudDetectEntity;
import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import com.example.frauddetection.Enum.RetryStatEnum;
import com.example.frauddetection.Enum.StatEnum;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.kafka.FraudDetectionProducer;
import com.example.frauddetection.repository.FraudDetectRepository;
import com.example.frauddetection.repository.FraudDetectRetryRepository;
import com.example.frauddetection.service.FraudDetectionRetryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = FraudDetectionApplication.class)
public class FraudDetectionRetryServiceTest {
    @Autowired
    private FraudDetectionRetryService fraudDetectionRetryService;

    @Autowired
    private FraudDetectRepository repository;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @Autowired
    private FraudDetectionProducer fraudDetectionProducer;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
        retryRepository.deleteAll();
    }

    @Test
    public void testDoRetry_normal() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId10086");
        transactionDTO.setUserId(101);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        FraudDetectRetryEntity retryEntity = fraudDetectionRetryService.initFraudDetectRetryEntityByTransaction(JSONObject.toJSONString(transactionDTO), transactionDTO, true);
        retryRepository.save(retryEntity);

        fraudDetectionRetryService.doRetry();
        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(entity));
        Assertions.assertTrue(StatEnum.PASS.getStat().equals(entity.getStat()));

        retryEntity = retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(retryEntity));
        Assertions.assertTrue(RetryStatEnum.SUCCESS.getRetryStat().equals(retryEntity.getRetryStat()));
        Assertions.assertTrue(1 == retryEntity.getRetryTimes());

    }

    @Test
    public void testDoRetry_exceedMax() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId10087");
        transactionDTO.setUserId(101);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        FraudDetectRetryEntity retryEntity = fraudDetectionRetryService.initFraudDetectRetryEntityByTransaction(JSONObject.toJSONString(transactionDTO), transactionDTO, true);
        retryEntity.setRetryTimes(10);
        retryRepository.save(retryEntity);

        fraudDetectionRetryService.doRetry();
        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.isNull(entity));

        retryEntity = retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(retryEntity));
        Assertions.assertTrue(RetryStatEnum.INIT.getRetryStat().equals(retryEntity.getRetryStat()));
        Assertions.assertTrue(10 == retryEntity.getRetryTimes());
    }


    @Test
    public void testDoRetry_paraError_noRetry() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId10088");
        transactionDTO.setUserId(null);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        fraudDetectionProducer.sendTransaction(transactionDTO);
        TimeUnit.SECONDS.sleep(5);

        FraudDetectRetryEntity retryEntity1 = retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(retryEntity1));
        Assertions.assertTrue(RetryStatEnum.NO_RETRY.getRetryStat().equals(retryEntity1.getRetryStat()));
        Assertions.assertTrue(0 == retryEntity1.getRetryTimes());

        fraudDetectionRetryService.doRetry();

        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.isNull(entity));

        FraudDetectRetryEntity retryEntity2 = retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(retryEntity2));

        Assertions.assertTrue(Objects.equals(retryEntity1.getRetryStat(), retryEntity2.getRetryStat()));
        Assertions.assertTrue(Objects.equals(retryEntity1.getRetryTimes(), retryEntity2.getRetryTimes()));
    }
}
