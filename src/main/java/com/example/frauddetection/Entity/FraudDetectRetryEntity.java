package com.example.frauddetection.Entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fraud_detect_retry")
@Data
public class FraudDetectRetryEntity implements Serializable {
    private static final long serialVersionUID = 5051826169891498536L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "transaction_record")
    private String transactionRecord;

    @Column(name = "retry_stat")
    private Integer retryStat;

    @Column(name = "retry_times")
    private Integer retryTimes;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;
}
