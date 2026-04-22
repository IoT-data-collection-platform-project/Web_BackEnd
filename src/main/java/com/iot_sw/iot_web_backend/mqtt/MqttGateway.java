package com.iot_sw.iot_web_backend.mqtt;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;

// 브로커로 메시지를 발행하는 역할
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {
    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
}
