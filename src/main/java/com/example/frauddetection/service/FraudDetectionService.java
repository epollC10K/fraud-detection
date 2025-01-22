package com.example.frauddetection.service;

import com.example.frauddetection.Entity.FraudDetectEntity;
import com.example.frauddetection.Enum.StatEnum;
import com.example.frauddetection.dto.TransactionDTO;
import com.example.frauddetection.repository.FraudDetectRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@Slf4j
public class FraudDetectionService {
    @Value("${spring.fraud.detect.rules.max-amount}")
    private String maxAmount;

    @Value("${spring.fraud.detect.rules.user-id.blacklist}")
    private String userIdBlacklist;

    @Autowired
    private FraudDetectRepository repository;

    public Integer queryFraudDetectResultByTransactionId(String transactionId) {
        FraudDetectEntity entity = repository.findFraudDetectEntityByTransactionId(transactionId);
        if (Objects.nonNull(entity)) {
            return entity.getStat();
        }

        return null;
    }

    /**
     * Pair<Boolean, Boolean> 第一个Boolean是风控结果，第二个Boolean决定是否发告警（上游重复发送消息，后续不应该在发告警）
     * @return
     */
    public Pair<Boolean, Boolean> fraudDetect(TransactionDTO transactionDTO) {
        FraudDetectEntity existFraudDetect = repository.findFraudDetectEntityByTransactionId(transactionDTO.getTransactionId());
        if (Objects.nonNull(existFraudDetect)) {
            if (StatEnum.PASS.getStat().equals(existFraudDetect.getStat())) {
                return Pair.of(false, false);
            }

            if (StatEnum.REJECT.getStat().equals(existFraudDetect.getStat())) {
                return Pair.of(true, false);
            }
        }

        FraudDetectEntity insFraudDetectEntity = initFraudDetectEntityByTransaction(transactionDTO);
        repository.save(insFraudDetectEntity);

        // 简单规则，检查金额和黑名单用户
        if (Integer.valueOf(maxAmount) < transactionDTO.getAmount()) {
            log.error("transaction:{} 超过最大金额:{}", transactionDTO, maxAmount);
            insFraudDetectEntity.setStat(StatEnum.REJECT.getStat());
            insFraudDetectEntity.setUpdatedAt(System.currentTimeMillis());
            repository.save(insFraudDetectEntity);
            return Pair.of(true, true);
        }

        String[] forbiddenUserList = userIdBlacklist.split(",");
        for (String userId : forbiddenUserList) {
            if (Integer.valueOf(userId).equals(transactionDTO.getUserId())) {
                log.error("transaction:{} 命中用户黑名单:{}", transactionDTO, forbiddenUserList);
                insFraudDetectEntity.setStat(StatEnum.REJECT.getStat());
                insFraudDetectEntity.setUpdatedAt(System.currentTimeMillis());
                repository.save(insFraudDetectEntity);
                return Pair.of(true, true);
            }
        }

        insFraudDetectEntity.setStat(StatEnum.PASS.getStat());
        insFraudDetectEntity.setUpdatedAt(System.currentTimeMillis());
        repository.save(insFraudDetectEntity);

        return Pair.of(false, true);
    }

    private FraudDetectEntity initFraudDetectEntityByTransaction(TransactionDTO transactionDTO) {
        FraudDetectEntity fraudDetectEntity = new FraudDetectEntity();
        fraudDetectEntity.setTransactionId(transactionDTO.getTransactionId());
        fraudDetectEntity.setUserId(transactionDTO.getUserId());
        fraudDetectEntity.setAmount(transactionDTO.getAmount());
        fraudDetectEntity.setBussTime(transactionDTO.getBussTime());
        fraudDetectEntity.setIp(transactionDTO.getIp());

        fraudDetectEntity.setStat(StatEnum.INIT.getStat());
        fraudDetectEntity.setCreatedAt(System.currentTimeMillis());
        fraudDetectEntity.setUpdatedAt(System.currentTimeMillis());

        return fraudDetectEntity;
    }
}
