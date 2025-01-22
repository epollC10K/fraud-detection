package com.example.frauddetection.util;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutLogEventsResult;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AWSCloudWatchLogger {
//    @Value("${spring.awsCloudWatch.log.region}")
//    private String LOG_GROUP_REGION;
//
//    @Value("${spring.awsCloudWatch.log.group}")
//    private String LOG_GROUP_NAME;
//
//    @Value("${spring.awsCloudWatch.log.stream}")
//    private String LOG_STREAM_NAME;
//    private AWSLogs logsClient;
//    private String sequenceToken;
//
//    public AWSCloudWatchLogger() {
//        // 创建 AWSLogs 客户端
//        logsClient = AWSLogsClientBuilder.standard()
//                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
//                .withRegion(LOG_GROUP_REGION) // 根据实际情况修改区域
//                .build();
//    }
//
//    public void logMessage(String message) {
//        List<InputLogEvent> logEvents = new ArrayList<>();
//
//        // 创建日志事件，添加时间戳和消息
//        InputLogEvent logEvent = new InputLogEvent()
//                .withTimestamp(new Date().getTime())
//                .withMessage(message);
//        logEvents.add(logEvent);
//
//        PutLogEventsRequest putLogEventsRequest = new PutLogEventsRequest()
//                .withLogGroupName(LOG_GROUP_NAME)
//                .withLogStreamName(LOG_STREAM_NAME)
//                .withLogEvents(logEvents);
//
//        if (sequenceToken != null) {
//            putLogEventsRequest.withSequenceToken(sequenceToken);
//        }
//
//        try {
//            PutLogEventsResult result = logsClient.putLogEvents(putLogEventsRequest);
//            // 获取下一个序列令牌
//            sequenceToken = result.getNextSequenceToken();
//        } catch (Exception e) {
//            if (e.getMessage().contains("DataAlreadyAcceptedException")) {
//                // 处理序列令牌过期或不匹配的情况
//                String expectedSequenceToken = e.getMessage()
//                        .split("sequenceToken is: ")[1].split(" ")[0];
//                sequenceToken = expectedSequenceToken;
//                logMessage(message);
//            } else {
//                e.printStackTrace();
//            }
//        }
//    }
}
