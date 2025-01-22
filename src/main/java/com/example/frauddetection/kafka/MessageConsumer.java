package com.example.frauddetection.kafka;

import com.example.frauddetection.exception.ParaErrException;
import com.example.frauddetection.service.FraudDetectionRetryService;
import com.example.frauddetection.util.AWSCloudWatchLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public abstract class MessageConsumer {
    private ExecutorService kafkaConsumerThreadPool;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.topic}")
    private String topic;

    @Value("${spring.kafka.consumer.threadPool.num}")
    private String threadPoolNum;

    @Value("${spring.fraud.detect.demotion.switch}")
    private String demotionSwitch;

    @Autowired
    private FraudDetectionRetryService fraudDetectionRetryService;

    AWSCloudWatchLogger logger = new AWSCloudWatchLogger();

    @PostConstruct
    public void init() {
        initThreadPool();
        KafkaConsumer<String, String> consumer = createKafkaConsumer();
        startListening(consumer);
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

    private void initThreadPool() {
        if (null == kafkaConsumerThreadPool) {
            kafkaConsumerThreadPool = Executors.newFixedThreadPool(Integer.valueOf(threadPoolNum));
        }
    }

    private void startListening(KafkaConsumer<String, String> consumer) {
        kafkaConsumerThreadPool.submit(() -> {
            List<String> topics = new ArrayList<>();
            topics.add(topic);
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    try {
                        // consumeMsg有问题，打开降级开关，保存到RetryDetectFraud，靠task重试
                        if ("true".equals(demotionSwitch.toLowerCase())) {
                            log.info("consumerDemotionSwitch is true, demotion saveRetryDetectFraud, message:{}", message);
                            throw new Exception("consumerDemotionSwitch is true, demotion saveRetryDetectFraud");
                        }
                        consumeMsg(message);
                    } catch (ParaErrException e) {
                        // 如果是参数错误，就不用重试了
                        log.error("consumeMsg fail.", e);
                        fraudDetectionRetryService.saveRetryDetectFraud(message, false);
                    } catch (Exception e) {
                        // 其他错误，目前全部重试（可以继续根据exception细化处理逻辑）
                        log.error("consumeMsg fail.", e);
                        fraudDetectionRetryService.saveRetryDetectFraud(message, true);
                    }

                    consumer.commitSync();
                }
            }
        });
    }

    public abstract void consumeMsg(String msgData) throws Exception;
}
