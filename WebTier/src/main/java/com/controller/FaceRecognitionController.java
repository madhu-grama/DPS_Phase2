package com.controller;

import com.service.FileUploaderService;
import com.service.FaceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class FaceRecognitionController {

    @Autowired
    FileUploaderService fileUploader;

    @Autowired
    FaceRecognitionService faceRecognitionService;

    @RequestMapping(value = "healthCheck", method = RequestMethod.GET)
    public String checkHealth() {
        return "healthy";
    }

    @Transactional(timeout = 600)
    @RequestMapping(value = "faceRecognition", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String postBody(@RequestParam("myfile") MultipartFile file) throws IOException {

        // Puts the files to input S3 bucket and returns the S3 image URL
        String url = fileUploader.uploadRequestFileToS3(file);

	return faceRecognitionService.putRequestToInputQueue(url, file.getOriginalFilename());
    }
}
