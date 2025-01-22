package com.example.frauddetection.Enum;

public enum StatEnum {
    INIT(1),
    PASS(2),
    REJECT(3);

    private final Integer stat;

    StatEnum(Integer stat) {
        this.stat = stat;
    }
    public Integer getStat() {
        return this.stat;
    }
}
