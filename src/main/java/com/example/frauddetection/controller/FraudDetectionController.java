package com.example.frauddetection.controller;

import com.example.frauddetection.Enum.StatEnum;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.kafka.FraudDetectionProducer;
import com.example.frauddetection.service.FraudDetectionService;
import com.example.frauddetection.util.AWSCloudWatchLogger;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

@RestController
@Slf4j
public class FraudDetectionController {
    @Autowired
    private FraudDetectionProducer producer;
    @Autowired
    private FraudDetectionService fraudDetectionService;

    AWSCloudWatchLogger logger = new AWSCloudWatchLogger();

    private RateLimiter transLimiter = RateLimiter.create(10); // Allow 10 requests per second

    @PostMapping("/transactions")
    public ResponseEntity<?> processTransaction(@RequestBody TransactionDTO transactionDTO) {
        if (transLimiter.tryAcquire()) {
            if (!transactionDTO.checkTransaction()) {
                return ResponseEntity.ok("入参错误");
            }

            producer.sendTransaction(transactionDTO);
            return ResponseEntity.ok("Transaction processed");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
        }
    }

    @PostMapping("/query")
    public ResponseEntity<?> queryTransaction(@RequestBody TransactionDTO transactionDTO) {
        if (transLimiter.tryAcquire()) {
            if (Objects.isNull(transactionDTO.getTransactionId())) {
                return ResponseEntity.ok("入参错误");
            }

            Integer result = fraudDetectionService.queryFraudDetectResultByTransactionId(transactionDTO.getTransactionId());
            String  sResult = "";
            if (Objects.isNull(result)) {
                sResult = "transactionId: " + transactionDTO.getTransactionId() + " not exist";
            } else {
                sResult = StatEnum.PASS.getStat().equals(result)
                        ? "transactionId: "+ transactionDTO.getTransactionId() + " pass"
                        : "transactionId: "+ transactionDTO.getTransactionId() + " reject" ;
            }
            return ResponseEntity.ok(sResult);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
        }
    }
}