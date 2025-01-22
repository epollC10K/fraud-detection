package com.example.frauddetection;

import com.example.frauddetection.Entity.FraudDetectEntity;
import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import com.example.frauddetection.Enum.RetryStatEnum;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.kafka.FraudDetectionProducer;
import com.example.frauddetection.repository.FraudDetectRepository;
import com.example.frauddetection.repository.FraudDetectRetryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FraudDetectionApplication.class)
@TestPropertySource(properties = { "spring.fraud.detect.demotion.switch=true"})
public class MessageConsumerTest2 {

    @Autowired
    private FraudDetectionProducer fraudDetectionProducer;

    @Autowired
    private FraudDetectRepository repository;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @Autowired
    private Environment environment;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
        retryRepository.deleteAll();
    }

    // 降级开关打开
    @Test
    public void testConsumeMsg_demotionSwitchOn() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId203");
        transactionDTO.setUserId(111);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        fraudDetectionProducer.sendTransaction(transactionDTO);

        TimeUnit.SECONDS.sleep(5);

        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.isNull(entity));

        FraudDetectRetryEntity retryEntity = retryRepository.
                findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(retryEntity));
        Assertions.assertTrue(RetryStatEnum.INIT.getRetryStat().equals(retryEntity.getRetryStat()));
        Assertions.assertTrue(0 == retryEntity.getRetryTimes());
    }

}
