package com.service;

import com.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.Hashtable;

@Service
public class FaceRecognitionService {

    private static Hashtable<String, String> imageNameMap = new Hashtable<String, String>();

    @Autowired
    SQSService sqsService;

    public String putRequestToInputQueue(String url, String fileName) {

        int index = fileName.indexOf(".");
        String image = fileName.substring(0, index);
        if (imageNameMap.contains(image)) {
            imageNameMap.remove(image);
        }
        sqsService.sendMessage(url, Constants.INPUTQUEUENAME, 0);

        String result = readResponseFromOutputQueue(image);
        return result;
    }

    public String readResponseFromOutputQueue(String requestedImage) {

        while (true) {
            String predictedOutput = imageNameMap.get(requestedImage);
            String imageInQueue = "";

            if (predictedOutput == null) {
                Message responseQMsg = sqsService.receiveMessage(Constants.OUTPUTQUEUENAME, 20, 5);
                if (responseQMsg == null) {
                    continue;
                }

                String responseQBody = responseQMsg.body();
                int indexOfImage = responseQBody.indexOf(",");
                imageInQueue = responseQBody.substring(1, indexOfImage);
                predictedOutput = responseQBody.substring(indexOfImage + 1, responseQBody.length()-1);

                if (imageInQueue.equals(requestedImage)) {
                    sqsService.deleteMessage(responseQMsg, Constants.OUTPUTQUEUENAME);
                    return predictedOutput;
                } else {
                    imageNameMap.put(imageInQueue, predictedOutput);
                    sqsService.deleteMessage(responseQMsg, Constants.OUTPUTQUEUENAME);
                }
            } else {
                imageNameMap.remove(requestedImage);
                return predictedOutput;
            }
        }
    }
}
