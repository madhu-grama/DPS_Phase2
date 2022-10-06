package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.script.*;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class WebTierMain {
    public static void main(String[] args) throws IOException, ScriptException {

        String root = new File("").getAbsolutePath();
        // System.out.println(root);

        // Run the Script for Load Balancer
        String scriptPath = root + File.separator + "load_balancer.py";

        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath).inheritIO();
        SpringApplication.run(WebTierMain.class, args);
        Process process = processBuilder.start();

    }
}
