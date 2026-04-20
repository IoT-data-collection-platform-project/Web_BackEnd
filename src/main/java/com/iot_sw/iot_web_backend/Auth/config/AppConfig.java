package com.iot_sw.iot_web_backend.Auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${kma.http.connect-timeout:3s}")
    private Duration connectTimeout;

    @Value("${kma.http.read-timeout:10s}")
    private Duration readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Math.toIntExact(connectTimeout.toMillis()));
        requestFactory.setReadTimeout(Math.toIntExact(readTimeout.toMillis()));
        return new RestTemplate(requestFactory);
    }
}