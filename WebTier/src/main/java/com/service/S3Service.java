package com.service;

import com.Constants;
import com.accessor.S3Accessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.InputStream;

import static com.Constants.INPUT_BUCKET_NAME;

@Service
public class S3Service {

    @Autowired
    S3Accessor s3Accessor;

    private static final String CONTENT_TYPE = "image/jpg";

    /**
     * {@inheritDoc}
     * Saves the specified file to the specified bucket.
     * @param fileName
     * @param contentLength
     * @param contentStream
     * @param bucketName
     * @return
     */
    public String saveFileToS3(final String fileName, final int contentLength,
                               final InputStream contentStream, final String bucketName) {

        if(!ifBucketAlreadyExist(bucketName)) {
            s3Accessor.createNewBucket(bucketName);
        }

        String fileUrl = null;
        PutObjectResponse response = s3Accessor.putObject(buildPutRequest(fileName), RequestBody.fromInputStream(contentStream, contentLength));
        fileUrl = Constants.BASE_URL_S3 + "/" + fileName;

        return fileUrl;
    }

    /**
     * Creates a put object request to put an object into bucket using API calls.
     * @param fileName
     * @return
     */
    private PutObjectRequest buildPutRequest(final String fileName) {
        return PutObjectRequest.builder().bucket(INPUT_BUCKET_NAME).key(fileName).contentType(CONTENT_TYPE).build();
    }

    /**
     * This method checks if bucket already exists.
     * @param bucketName
     * @return
     */
    private boolean ifBucketAlreadyExist(final String bucketName) {
        return s3Accessor.getBuckets().stream().anyMatch(newObj -> newObj.name().equalsIgnoreCase(bucketName));
    }
}
