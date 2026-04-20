package com.iot_sw.iot_web_backend.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
// 💡 이 어노테이션이 있어야 아까 만든 @MessagingGateway를 스프링이 인식합니다!
@IntegrationComponentScan(basePackages = "com.iot_sw.iot_web_backend")
public class MqttConfig {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "spring-boot-server-outbound";

    // 1. MQTT 브로커 연결 설정
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { BROKER_URL });
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    // 2. 메시지가 나갈 통로(Channel) 생성 (MqttGateway에서 지정한 채널명과 똑같아야 함!)
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 3. 실제로 메시지를 브로커로 발송하는 핸들러
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(CLIENT_ID, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("default/topic"); // 기본 토픽 (보낼 때 덮어씌워짐)
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    // 💡 2. 브로커에서 메시지를 빨아들이는 어댑터(구독자) 설정
    @Bean
    public MessageProducer inbound() {
        // "spring-boot-server-inbound" 라는 이름으로 접속하여,
        // "provisioning/request" 토픽과 "telemetry/#" (센서 데이터 전체) 토픽을 구독합니다!
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("spring-boot-server-inbound", mqttClientFactory(),
                        "provisioning/request", "telemetry/#");

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1); // 메시지 전달 보장 수준 설정
        adapter.setOutputChannel(mqttInboundChannel()); // 빨아들인 메시지를 위의 통로로 던짐
        return adapter;
    }
}
