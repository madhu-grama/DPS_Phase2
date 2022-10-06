package com.service;

import com.accessor.SQSAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class SQSService {

    @Autowired
    SQSAccessor sqsAccessor;

    public void sendMessage(String messageBody, String queueName, Integer delayTime) {
        sqsAccessor.sendMessage(messageBody, queueName, delayTime);
    }

    public Message receiveMessage(String queueName, Integer waitTime, Integer visibilityTimeout) {
        return sqsAccessor.receiveMessage(queueName, waitTime, visibilityTimeout) ;
    }

    public void deleteMessage(Message messageContent, String queueName) {
        sqsAccessor.deleteMessage(messageContent, queueName);
    }

    public void createQueue(String queueName) {
         sqsAccessor.createQueue(queueName);
    }
}
