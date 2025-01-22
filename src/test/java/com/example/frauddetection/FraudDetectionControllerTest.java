package com.example.frauddetection;

import com.example.frauddetection.Entity.FraudDetectEntity;
import com.example.frauddetection.Entity.FraudDetectRetryEntity;
import com.example.frauddetection.Enum.StatEnum;
import com.example.frauddetection.repository.FraudDetectRepository;
import com.example.frauddetection.repository.FraudDetectRetryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest(classes = FraudDetectionApplication.class)
@AutoConfigureMockMvc
public class FraudDetectionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FraudDetectRepository repository;

    @Autowired
    private FraudDetectRetryRepository retryRepository;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
    }

    @Test
    public void testController_transactions() throws Exception {
        String json = "{\"transactionId\":\"transactionId401\", \"userId\": 111, \"amount\": 100}";
        mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string("Transaction processed"));

        TimeUnit.SECONDS.sleep(5);

        FraudDetectEntity existEntity = repository.findFraudDetectEntityByTransactionId("transactionId401");
        Assertions.assertTrue(Objects.nonNull(existEntity));
        Assertions.assertTrue(StatEnum.PASS.getStat().equals(existEntity.getStat()));

        FraudDetectRetryEntity retryEntity = retryRepository.findFraudDetectRetryEntitiesByTransactionId("transactionId401");
        Assertions.assertTrue(Objects.isNull(retryEntity));
    }

    @Test
    public void testController_query_hasRecord() throws Exception {
        // 构造数据库数据
        FraudDetectEntity entity = new FraudDetectEntity();
        entity.setTransactionId("transactionId402");
        entity.setUserId(111);
        entity.setAmount(100);
        entity.setBussTime(System.currentTimeMillis() - 10000);
        entity.setStat(StatEnum.PASS.getStat());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        repository.save(entity);

        String json = "{\"transactionId\":\"transactionId402\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string("transactionId: transactionId402 pass"));
    }

    @Test
    public void testController_query_noRecord() throws Exception {
        String json = "{\"transactionId\":\"transactionId403\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string("transactionId: transactionId403 not exist"));
    }
}
