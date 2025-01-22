DROP TABLE IF EXISTS fraud_detect;
CREATE TABLE fraud_detect(
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    user_id int(11) NOT NULL,
    amount int(11) NOT NULL,
    ip VARCHAR(16),
    /* 业务发生时间*/
    buss_time int(11),
    stat int(11) NOT NULL,
    created_at int(11) NOT NULL,
    updated_at int(11) NOT NULL,
    UNIQUE INDEX uk_transaction_id(transaction_id)
);

DROP TABLE IF EXISTS fraud_detect_retry;
CREATE TABLE fraud_detect_retry (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    transaction_record VARCHAR(1024) NOT NULL,
    retry_stat int(11) NOT NULL,
    retry_times int(11) NOT NULL,
    created_at int(11) NOT NULL,
    updated_at int(11) NOT NULL,
    UNIQUE INDEX uk_transaction_id_retry(transaction_id)
);