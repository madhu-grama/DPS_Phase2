package com.config;

import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import static com.Constants.*;

@Configuration
public class AWSConfiguration {

    private static AwsBasicCredentials getAWSBaseCredentials() {
        return AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
    }

    public static S3Client s3Client() {
        S3Client amazonS3Client = S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(getAWSBaseCredentials()))
                .build();
        return amazonS3Client;
    }

    public static SqsClient sqsClient() {
        SqsClient amazonSQSClient = SqsClient.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(getAWSBaseCredentials()))
                .build();
        return amazonSQSClient;
    }
}