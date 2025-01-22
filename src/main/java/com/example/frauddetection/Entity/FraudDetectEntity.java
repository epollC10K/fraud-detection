package com.example.frauddetection.Entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fraud_detect")
@Data
public class FraudDetectEntity implements Serializable {

    private static final long serialVersionUID = 8591420984758095185L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "transaction_id",  nullable = false, unique = true)
    private String transactionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "ip")
    private String ip;

    @Column(name = "buss_time")
    private Long bussTime;

    @Column(name = "stat")
    private Integer stat;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;
}
