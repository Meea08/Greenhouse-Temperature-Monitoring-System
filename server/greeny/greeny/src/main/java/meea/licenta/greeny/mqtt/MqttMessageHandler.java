package meea.licenta.greeny.mqtt;

import meea.licenta.greeny.entities.SensorLog;
import meea.licenta.greeny.repositories.ComponentRepository;
import meea.licenta.greeny.services.SensorLogService;
import meea.licenta.greeny.websocket.SensorLogWebSocketController;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class MqttMessageHandler implements MqttCallback {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private SensorLogService sensorLogService;

    @Autowired
    private SensorLogWebSocketController webSocketController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void connectionLost(Throwable cause) {
        // Handle lost connection
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String payload = new String(message.getPayload());
            JsonNode jsonNode = objectMapper.readTree(payload);

            String deviceId = jsonNode.get("id").asText();
            long timestamp = jsonNode.get("timestamp").asLong();
            JsonNode temperatures = jsonNode.get("temperatures");

            List<SensorLog> sensorLogsToBroadcast = new ArrayList<>();
            for (int i = 0; i < temperatures.size(); i++) {
                float temperatureValue = temperatures.get(i).floatValue();
                String sensorName = "Temperature Sensor " + (i + 1);

                meea.licenta.greeny.entities.component.Component sensor = componentRepository.findByNameAndControllerId(sensorName, Integer.parseInt(deviceId));
                if (sensor == null) {
                    continue;
                }

                if (sensor.isActive()) {
                    SensorLog sensorLog = new SensorLog();
                    sensorLog.setComponent(sensor);
                    sensorLog.setTimestamp(new Timestamp(timestamp * 1000)); // Convert to milliseconds
                    sensorLog.setValue(temperatureValue);

                    SensorLog createdLog = sensorLogService.saveSensorLog(sensorLog);

                    if (createdLog != null) {
                        sensorLogsToBroadcast.add(createdLog);  // Collect logs to broadcast later
                    }
                }
            }

            // After the loop, broadcast all collected sensor logs at once
            if (!sensorLogsToBroadcast.isEmpty()) {
                webSocketController.broadcastAllSensorLogs(sensorLogsToBroadcast);  // Broadcast all logs in a single message
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

}
