package com.example.frauddetection.kafka;

import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.exception.ParaErrException;
import com.example.frauddetection.service.FraudDetectionService;
import com.example.frauddetection.util.EmailNotify;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class FraudDetectionConsumer  extends MessageConsumer {
    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private EmailNotify emailNotify;

    @Value("${spring.fraud.detect.sendAlert.threadPool.num}")
    private String sendAlertTheadPoolNum;
    private ExecutorService sendAlertThreadPool;

    private RateLimiter transLimiter = RateLimiter.create(2);

    @PostConstruct
    public void initThreadPool() {
        if (null == sendAlertThreadPool) {
            sendAlertThreadPool = Executors.newFixedThreadPool(Integer.valueOf(sendAlertTheadPoolNum));
        }
    }

    @Override
    public void consumeMsg(String msgData) throws Exception {
        // 限频
        if (!transLimiter.tryAcquire()) {
            log.error("consumeMsg message：{} fail, too busy", msgData);
            throw new Exception("too busy error");
        }

        TransactionDTO transactionDTO = JSONObject.parseObject(msgData, TransactionDTO.class);

        // 入参校验
        if (!transactionDTO.checkTransaction()) {
            log.error("consumeMsg message：{} fail, input parameter error", msgData);
            throw new ParaErrException("input parameter error");
        }

        Pair<Boolean, Boolean> fraudResult = fraudDetectionService.fraudDetect(transactionDTO);

        if (fraudResult.getLeft() && fraudResult.getRight()) {
            log.error("detectFraud transaction: {}", transactionDTO.getTransactionId());
            // 不影响主流程，异步发送告警
            sendAlertThreadPool.submit(() -> {
                emailNotify.sendFraudAlert(transactionDTO);
            });
        }
    }
}
