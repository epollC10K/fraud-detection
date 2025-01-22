package com.example.frauddetection.exception;

public class ParaErrException extends Exception {
    public ParaErrException(String message) {
        super(message); // 将自定义错误信息传递给父类
    }
}
