package meea.licenta.greeny.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import meea.licenta.greeny.entities.SensorLog;
import meea.licenta.greeny.services.SensorLogService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Controller
public class SensorLogWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private SensorLogService sensorLogService;

    public SensorLogWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sensor-logs")
    @SendTo("/topic/sensor-logs")
    public SensorLog sendSensorLog(SensorLog sensorLog) {
        return sensorLog;
    }

    // SensorLogWebSocketController.java
    public void broadcastSensorLog(SensorLog sensorLog) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode logJson = objectMapper.createObjectNode();

            // Use sensor ID instead of log ID
            logJson.put("sensorId", sensorLog.getComponent().getId());
            logJson.put("timestamp", sensorLog.getTimestamp().getTime() / 1000); // Convert to seconds
            logJson.put("value", sensorLog.getValue());

            String message = objectMapper.writeValueAsString(logJson);
            this.messagingTemplate.convertAndSend("/topic/sensor-logs", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastAllSensorLogs(List<SensorLog> sensorLogs) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode logsArray = objectMapper.createArrayNode();

            for (SensorLog sensorLog : sensorLogs) {
                ObjectNode logJson = objectMapper.createObjectNode();
                logJson.put("sensorId", sensorLog.getComponent().getId());
                logJson.put("timestamp", sensorLog.getTimestamp().getTime() / 1000); // Convert to seconds
                logJson.put("value", sensorLog.getValue());
                logsArray.add(logJson);
            }

            String message = objectMapper.writeValueAsString(logsArray);
            this.messagingTemplate.convertAndSend("/topic/sensor-logs", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
