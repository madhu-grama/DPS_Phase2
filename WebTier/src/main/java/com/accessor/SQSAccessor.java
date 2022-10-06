package com.accessor;

import com.config.AWSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Repository
public class SQSAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSAccessor.class);

    // Send the required message into the specified SQS queue
    public void sendMessage(String messageContent, String queueName, Integer delayTime) {

        //LOGGER.debug("Sending the message into the queue.");

        String queueUrl = this.getQueueUrl(queueName);

        AWSConfiguration.sqsClient().sendMessage(SendMessageRequest.builder().queueUrl(queueUrl)
                                    .messageBody(messageContent).delaySeconds(delayTime).build());
    }

    // Gets the specified SQS queue URL. Makes API call to get the queue URL.
    public String getQueueUrl(final String queueName) {
        String queueUrl = null;
        try {
            GetQueueUrlRequest queueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
            GetQueueUrlResponse urlRequestResponse = AWSConfiguration.sqsClient().getQueueUrl(queueUrlRequest);
            queueUrl = urlRequestResponse.queueUrl();
        } catch (QueueDoesNotExistException queueDoesNotExistException) {
            CreateQueueResponse createQueueResponse = this.createQueue(queueName);
            queueUrl = createQueueResponse.queueUrl();
        } catch (SqsException sqsException) {
            throw new RuntimeException(sqsException);
        }
        return queueUrl;
    }

    // Gets a message from the specified queue. Makes API call to get the message.
    public Message receiveMessage(String queueName, Integer waitTime, Integer visibilityTimeout) {

        //LOGGER.debug("Receiving the message from the queue.");

        String queueUrl = this.getQueueUrl(queueName);

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl)
                .maxNumberOfMessages(1).waitTimeSeconds(waitTime).visibilityTimeout(visibilityTimeout).build();
        List<Message> receivedMessage = AWSConfiguration.sqsClient().receiveMessage(receiveMessageRequest).messages();
        if(!receivedMessage.isEmpty()) {
            return receivedMessage.get(0);
        }

        return null;
    }

    // Deletes the specified message from the specified queue. Makes the API call to delete the message from queue.
    public void deleteMessage(Message messageContent, String queueName) {

        String queueUrl = this.getQueueUrl(queueName);

        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
                .receiptHandle(messageContent.receiptHandle()).build();
        AWSConfiguration.sqsClient().deleteMessage(deleteMessageRequest);
    }

    // Creates a new queue with the specified name. Makes API call to create the queue.
    public CreateQueueResponse createQueue(final String queueName) {

        LOGGER.debug("Creating the queue.");

        CreateQueueRequest newQueueRequest = CreateQueueRequest.builder().queueName(queueName).build();
        CreateQueueResponse createQueueResponse = AWSConfiguration.sqsClient().createQueue(newQueueRequest);
        return createQueueResponse;
    }
}
