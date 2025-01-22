package com.example.frauddetection;

import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.util.EmailNotify;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FraudDetectionApplication.class)
public class EmailNotifyTest {
    @Autowired
    private EmailNotify emailNotify;
    @Test
    public void test_sendFraudAlert() {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId("Transaction111");
        dto.setAmount(1000000);
        dto.setUserId(111);
        emailNotify.sendFraudAlert(dto);
    }
}
