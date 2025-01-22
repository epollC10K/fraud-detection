package com.example.frauddetection;

import com.example.frauddetection.Entity.FraudDetectEntity;
import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import com.example.frauddetection.Enum.RetryStatEnum;
import com.example.frauddetection.Enum.StatEnum;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FraudDetectionApplication.class)
public class MessageConsumerTest {

    @Autowired
    private FraudDetectionProducer fraudDetectionProducer;

    @Autowired
    private FraudDetectRepository repository;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
        retryRepository.deleteAll();
    }

    // 正常
    @Test
    public void testConsumeMsg_normal_pass() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId201");
        transactionDTO.setUserId(111);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        fraudDetectionProducer.sendTransaction(transactionDTO);

        TimeUnit.SECONDS.sleep(5);

        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(entity));
        Assertions.assertTrue(entity.getStat().equals(StatEnum.PASS.getStat()));

        FraudDetectRetryEntity retryEntity =
                retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue( Objects.isNull(retryEntity));
    }

    // 风控失败：超过金额
    @Test
    public void testDetectFraud_WhenAmountIsHigh() throws Exception {
        // 防止报并发的错误，sleep一下
        TimeUnit.SECONDS.sleep(1);

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId202");
        transactionDTO.setUserId(1);
        transactionDTO.setAmount(1000000);
        transactionDTO.setBussTime(System.currentTimeMillis());

        fraudDetectionProducer.sendTransaction(transactionDTO);

        TimeUnit.SECONDS.sleep(6);

        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue( Objects.nonNull(entity));
        Assertions.assertTrue(entity.getStat().equals(StatEnum.REJECT.getStat()));

        FraudDetectRetryEntity retryEntity =
                retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue( Objects.isNull(retryEntity));
    }

    // 在黑名单
    @Test
    public void testDetectFraud_UserBlackListIn() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId203");
        transactionDTO.setUserId(123);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        fraudDetectionProducer.sendTransaction(transactionDTO);

        TimeUnit.SECONDS.sleep(5);

        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(entity));
        Assertions.assertTrue(entity.getStat().equals(StatEnum.REJECT.getStat()));

        FraudDetectRetryEntity retryEntity =
                retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.isNull(retryEntity));
    }

    // 超过限流
    @Test
    public void testConsumeMsg_rateLimiter_checkHasRecord() throws Exception {
        TransactionDTO transaction = new TransactionDTO();
        transaction.setTransactionId("transactionId204");
        transaction.setUserId(111);
        transaction.setAmount(100);
        transaction.setBussTime(System.currentTimeMillis());

        transaction.setTransactionId("transactionId205");
        fraudDetectionProducer.sendTransaction(transaction);

        transaction.setTransactionId("transactionId206");
        fraudDetectionProducer.sendTransaction(transaction);

        transaction.setTransactionId("transactionId207");
        fraudDetectionProducer.sendTransaction(transaction);

        transaction.setTransactionId("transactionId208");
        fraudDetectionProducer.sendTransaction(transaction);

        transaction.setTransactionId("transactionId209");
        fraudDetectionProducer.sendTransaction(transaction);

        TimeUnit.SECONDS.sleep(2);

        List<FraudDetectRetryEntity> retryEntitys = retryRepository.
                findFraudDetectRetryEntitiesByRetryStatEqualsAndRetryTimesLessThanAndUpdatedAtGreaterThan(
                        RetryStatEnum.INIT.getRetryStat(), 10,
                        System.currentTimeMillis() - 600000
                );

        Assertions.assertTrue(!CollectionUtils.isEmpty(retryEntitys));
        for (FraudDetectRetryEntity retryEntity : retryEntitys) {
            Assertions.assertTrue(RetryStatEnum.INIT.getRetryStat().equals(retryEntity.getRetryStat()));
            Assertions.assertTrue(0 == retryEntity.getRetryTimes());
        }
    }

    // 入参错误
    @Test
    public void testConsumeMsg_inputError_checkHasRecord() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId210");
        transactionDTO.setUserId(null);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        fraudDetectionProducer.sendTransaction(transactionDTO);

        TimeUnit.SECONDS.sleep(5);

        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.isNull(entity));

        FraudDetectRetryEntity retryEntity = retryRepository.
                findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(retryEntity));
        Assertions.assertTrue(RetryStatEnum.NO_RETRY.getRetryStat().equals(retryEntity.getRetryStat()));
        Assertions.assertTrue(0 == retryEntity.getRetryTimes());
    }

    // 测试数据库已有记录，记录是PASS，幂等
    @Test
    public void testConsumeMsg_idempotentPass() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId211");
        transactionDTO.setUserId(101);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        FraudDetectEntity entity = new FraudDetectEntity();
        entity.setTransactionId(transactionDTO.getTransactionId());
        entity.setUserId(transactionDTO.getUserId());
        entity.setAmount(transactionDTO.getAmount());
        entity.setBussTime(transactionDTO.getBussTime());
        entity.setStat(StatEnum.PASS.getStat());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        repository.save(entity);

        fraudDetectionProducer.sendTransaction(transactionDTO);
        TimeUnit.SECONDS.sleep(5);
        FraudDetectEntity existEntity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(existEntity));

        Assertions.assertTrue(entity.getStat().equals(existEntity.getStat()));
        Assertions.assertTrue(entity.getUpdatedAt().equals(existEntity.getUpdatedAt()));
    }

    // 测试数据库已有记录，记录是reject，幂等
    @Test
    public void testConsumeMsg_idempotentReject() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId212");
        transactionDTO.setUserId(101);
        transactionDTO.setAmount(10000000);
        transactionDTO.setBussTime(System.currentTimeMillis());

        FraudDetectEntity entity = new FraudDetectEntity();
        entity.setTransactionId(transactionDTO.getTransactionId());
        entity.setUserId(transactionDTO.getUserId());
        entity.setAmount(transactionDTO.getAmount());
        entity.setBussTime(transactionDTO.getBussTime());
        entity.setStat(StatEnum.REJECT.getStat());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        repository.save(entity);

        fraudDetectionProducer.sendTransaction(transactionDTO);
        TimeUnit.SECONDS.sleep(5);

        FraudDetectEntity existEntity = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        Assertions.assertTrue(Objects.nonNull(existEntity));

        Assertions.assertTrue(entity.getStat().equals(existEntity.getStat()));
        Assertions.assertTrue(entity.getUpdatedAt().equals(existEntity.getUpdatedAt()));
    }
}
