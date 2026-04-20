package com.iot_sw.iot_web_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IoTWebBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IoTWebBackendApplication.class, args);
    }

}
