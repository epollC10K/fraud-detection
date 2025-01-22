package com.example.frauddetection.util;

import com.alibaba.fastjson.JSONObject;
import com.example.frauddetection.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Service
public class EmailNotify {
    @Value("${spring.mail.smtp.host}")
    private String smtpHost;

    @Value("${spring.mail.fromAccount}")
    private String fromAcct;

    @Value("${spring.mail.toAccount}")
    private String toAcct;

    public void sendFraudAlert(TransactionDTO transactionDTO) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(fromAcct, "knqhjenkzfnnbidb");
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAcct));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAcct));
            msg.setSubject("Fraud Detection Alert");
            String text = "Fraud Detection Alert, transaction:" + JSONObject.toJSONString(transactionDTO);
            msg.setText(text);
            Transport.send(msg);
        } catch (Exception e) {
            log.error("sendFraudAlert fail, ", e);
            e.printStackTrace();
        }
    }
}
