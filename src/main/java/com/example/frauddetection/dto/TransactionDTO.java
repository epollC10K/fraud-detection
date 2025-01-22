package com.example.frauddetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO implements Serializable {
    private static final long serialVersionUID = -9154079885009449207L;

    private String transactionId;
    private Integer userId;
    private Integer amount;
    private String ip;
    private Long bussTime;

    public Boolean checkTransaction() {
        return !StringUtils.isEmpty(this.transactionId)
                && !Objects.isNull(this.userId)
                && !Objects.isNull(this.amount);
    }
}
