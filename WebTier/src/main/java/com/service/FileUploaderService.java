package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.Constants.INPUT_BUCKET_NAME;

@Service
public class FileUploaderService {

    @Autowired
    S3Service s3Service;

    // Handle the file upload to S3 bucket
    public String uploadRequestFileToS3(final MultipartFile file) throws IOException {
        byte[] streamContent = IoUtils.toByteArray(file.getInputStream());
        InputStream ioStream = new ByteArrayInputStream(streamContent);
        String fileName = file.getOriginalFilename();
        return s3Service.saveFileToS3(fileName, streamContent.length, ioStream, INPUT_BUCKET_NAME);
    }
}
