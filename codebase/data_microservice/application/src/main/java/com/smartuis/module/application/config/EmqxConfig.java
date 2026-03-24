package com.smartuis.module.application.config;

import com.smartuis.module.application.mqtt.EmqxListener;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmqxConfig {


    @Value("${mqtt.broker.url}")
    private String BROKER_URL;
    @Value("${mqtt.broker.client-id}")
    private String CLIENT_ID;
    @Value("${topic.listener}")
    private String TOPIC;
    private EmqxListener emqxListener;

    public EmqxConfig(EmqxListener emqxListener) {
        this.emqxListener = emqxListener;
    }

    @Bean
    public MqttClient initializeMqttClient() {
        MqttClient client = null;
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.setCallback(emqxListener);
            client.connect();

            MqttSubscription[] subscriptions = {new MqttSubscription(TOPIC, 1)};
            client.subscribe(subscriptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return client;
    }
}
