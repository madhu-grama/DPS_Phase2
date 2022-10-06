package com.accessor;

import com.config.AWSConfiguration;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.util.List;

import static com.Constants.INPUT_BUCKET_NAME;

@Repository
public class S3Accessor {

    // Member variable hardcoded to store the policy required to access the bucket
    private static final String BUCKET_POLICY_TEXT = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Id\": \"Policy1647405246208\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"Stmt1647405244232\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": \"*\",\n" +
            "            \"Action\": \"s3:GetObject\",\n" +
            "            \"Resource\": \"arn:aws:s3:::" + INPUT_BUCKET_NAME + "/*\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    // Method to create a new bucket if the bucket doesn't exist. Performs API calls to create a bucket.
    public void createNewBucket(final String S3bucketName) {
        try {
            S3Waiter s3Waiter = AWSConfiguration.s3Client().waiter();
            CreateBucketRequest newBucketRequest = CreateBucketRequest.builder().bucket(S3bucketName).build();

            AWSConfiguration.s3Client().createBucket(newBucketRequest);
            HeadBucketRequest bucketCreator = HeadBucketRequest.builder().bucket(S3bucketName).build();

            // Wait until the bucket is created and print out the response
            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketCreator);
            waiterResponse.matched().response().ifPresent(System.out::println);
            //System.out.println(S3bucketName + " is ready");

        } catch (S3Exception e) {
            e.printStackTrace();
        }

        try {
            PutBucketPolicyRequest bucketPolicyRequest = PutBucketPolicyRequest.builder().bucket(INPUT_BUCKET_NAME)
                    .policy(BUCKET_POLICY_TEXT).build();

            AWSConfiguration.s3Client().putBucketPolicy(bucketPolicyRequest);
        } catch (S3Exception e) {
            e.printStackTrace();
        }
    }

    // Gives all the buckets that currently exist
    public List<Bucket> getBuckets() {
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = AWSConfiguration.s3Client().listBuckets(listBucketsRequest);
        return listBucketsResponse.buckets();
    }

    // Puts the desired object into the bucket. Uses API calls to put the object into S3 bucket.
    public PutObjectResponse putObject(final PutObjectRequest objectRequest, final RequestBody requestBody) {
        PutObjectResponse putObjectResponse = null;
        try {
            putObjectResponse = AWSConfiguration.s3Client().putObject(objectRequest, requestBody);
        } catch (S3Exception s3Exception) {
            s3Exception.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return putObjectResponse;
    }
}
