package com.example.frauddetection.kafka;

import com.example.frauddetection.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FraudDetectionProducer {
    @Autowired
    private KafkaTemplate<String, TransactionDTO> kafkaTemplate;

    @Value("${spring.kafka.topic}")
    private String topic;

    public void sendTransaction(TransactionDTO transactionDTO) {
        kafkaTemplate.send(topic, transactionDTO);
        log.info("sendTransaction success， message：{} ", transactionDTO);
    }
}
