package com.example.frauddetection;

import com.alibaba.fastjson.JSONObject;
import com.example.frauddetection.kafka.FraudDetectionConsumer;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.repository.FraudDetectRepository;
import com.example.frauddetection.repository.FraudDetectRetryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest(classes = FraudDetectionApplication.class)
public class FraudDetectionConsumerTest {
    @Autowired
    private FraudDetectionConsumer fraudDetectionConsumer;

    @Autowired
    private FraudDetectRepository repository;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
        retryRepository.deleteAll();
    }

    // 超过限流，校验异常
    @Test
    public void testConsumeMsg_rateLimiter_checkException() throws Exception {
        // 防止报并发的错误，sleep一下
        TimeUnit.SECONDS.sleep(1);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId01");
        transactionDTO.setUserId(111);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        try {
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));

            transactionDTO.setTransactionId("transactionId02");
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));

            transactionDTO.setTransactionId("transactionId03");
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));

            transactionDTO.setTransactionId("transactionId04");
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));

            transactionDTO.setTransactionId("transactionId05");
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));

            transactionDTO.setTransactionId("transactionId06");
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));
        } catch (Exception e) {
            Assertions.assertEquals("too busy error", e.getMessage());
        }
    }

    // 入参错误，校验异常
    @Test
    public void testConsumeMsg_inputError_checkException() throws Exception {
        // 防止报并发的错误，sleep一下
        TimeUnit.SECONDS.sleep(1);

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setTransactionId("transactionId01");
        transactionDTO.setUserId(null);
        transactionDTO.setAmount(100);
        transactionDTO.setBussTime(System.currentTimeMillis());

        try {
            fraudDetectionConsumer.consumeMsg(JSONObject.toJSONString(transactionDTO));
        } catch (Exception e) {
            Assertions.assertEquals("input parameter error", e.getMessage());
        }
    }
}
