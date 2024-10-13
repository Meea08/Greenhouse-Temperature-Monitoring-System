package meea.licenta.greeny.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    @Autowired
    private MqttMessageHandler mqttMessageHandler;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        String broker = "ssl://e5dfb536a0a1462aa9e5c9a81159c869.s1.eu.hivemq.cloud:8883";
        String clientId = "spring-boot-client";
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient client = new MqttClient(broker, clientId, persistence);

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName("server_client_1");
        connOpts.setPassword("Server_client_1".toCharArray());
        connOpts.setCleanSession(true);

        System.out.println("Connecting to broker: " + broker);
        client.connect(connOpts);
        System.out.println("Connected");

        client.setCallback(mqttMessageHandler);

        System.out.println("Subscribing to topic: esp8266/temperature");
        client.subscribe("esp8266/temperature");
        System.out.println("Subscribed to topic: esp8266/temperature");

        return client;
    }

}
