package com.example.frauddetection.service;

import com.alibaba.fastjson.JSONObject;
import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import com.example.frauddetection.Enum.RetryStatEnum;
import com.example.frauddetection.kafka.MessageConsumer;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.repository.FraudDetectRepository;
import com.example.frauddetection.repository.FraudDetectRetryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FraudDetectionRetryService {
    @Autowired
    private MessageConsumer messageConsumer;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @Autowired
    private FraudDetectRepository repository;

    public void saveRetryDetectFraud(String message, Boolean hasRetry) {
        TransactionDTO transactionDTO = JSONObject.parseObject(message, TransactionDTO.class);

        FraudDetectRetryEntity existRetryEntity = retryRepository.findFraudDetectRetryEntitiesByTransactionId(transactionDTO.getTransactionId());
        if (Objects.nonNull(existRetryEntity)) {
            return;
        }

        FraudDetectRetryEntity retryEntity = initFraudDetectRetryEntityByTransaction(message, transactionDTO, hasRetry);
        retryRepository.save(retryEntity);
    }

    public FraudDetectRetryEntity initFraudDetectRetryEntityByTransaction(String message, TransactionDTO transactionDTO, Boolean hasRetry) {
        FraudDetectRetryEntity retryEntity = new FraudDetectRetryEntity();
        retryEntity.setTransactionId(transactionDTO.getTransactionId());
        retryEntity.setTransactionRecord(message);
        if (hasRetry) {
            retryEntity.setRetryStat(RetryStatEnum.INIT.getRetryStat());
        } else {
            retryEntity.setRetryStat(RetryStatEnum.NO_RETRY.getRetryStat());
        }
        retryEntity.setRetryTimes(0);
        retryEntity.setCreatedAt(System.currentTimeMillis());
        retryEntity.setUpdatedAt(System.currentTimeMillis());

        return retryEntity;
    }

    /**
     * 1分钟跑一次，取数条件：状态为INIT、不超过最大次数（10次），updatedAt一个小时以前
    */
    //@Scheduled(fixedRate = 600000)
    public void doRetry() {
        Long updatedAtPos = System.currentTimeMillis() - 3600*1000;
        List<FraudDetectRetryEntity> retryEntities = retryRepository.findFraudDetectRetryEntitiesByRetryStatEqualsAndRetryTimesLessThanAndUpdatedAtGreaterThan(
                RetryStatEnum.INIT.getRetryStat(), 10, updatedAtPos
        );

        if (Objects.nonNull(retryEntities)) {
            log.info("doRetry start, time:{}", System.currentTimeMillis()/1000);
            for (FraudDetectRetryEntity retryEntity : retryEntities) {
                log.info("doRetry retryEntity:{}", retryEntity);
                try {
                    messageConsumer.consumeMsg(retryEntity.getTransactionRecord());
                    retryEntity.setRetryStat(RetryStatEnum.SUCCESS.getRetryStat());
                    retryEntity.setRetryTimes(retryEntity.getRetryTimes() + 1);
                    retryEntity.setUpdatedAt(System.currentTimeMillis());
                    retryRepository.save(retryEntity);
                } catch (Exception e) {
                    log.error("doRetry retryEntity fail ", e);
                    retryEntity.setRetryTimes(retryEntity.getRetryTimes() + 1);
                    retryEntity.setUpdatedAt(System.currentTimeMillis());
                    retryRepository.save(retryEntity);
                }
            }
        }

        log.info("doRetry end, time:{}", System.currentTimeMillis()/1000);
    }
}
