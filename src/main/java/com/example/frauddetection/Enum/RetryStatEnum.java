package com.example.frauddetection.Enum;

public enum RetryStatEnum {
    INIT(1),
    SUCCESS(2),
    NO_RETRY(3);

    private final Integer retryStat;

    RetryStatEnum(Integer retryStat) {
        this.retryStat = retryStat;
    }
    public Integer getRetryStat() {
        return this.retryStat;
    }
}
