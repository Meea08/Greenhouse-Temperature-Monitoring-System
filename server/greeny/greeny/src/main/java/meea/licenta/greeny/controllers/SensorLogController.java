package meea.licenta.greeny.controllers;

import meea.licenta.greeny.entities.SensorLog;
import meea.licenta.greeny.services.SensorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensorlogs")
public class SensorLogController {

    private final SensorLogService sensorLogService;

    @Autowired
    public SensorLogController(SensorLogService sensorLogService) {
        this.sensorLogService = sensorLogService;
    }

    @GetMapping("/{sensorId}")
    public ResponseEntity<List<SensorLog>> getSensorLogs(
            @PathVariable Integer sensorId) {
        List<SensorLog> logs = sensorLogService.getLogsBySensorId(sensorId);
        return ResponseEntity.ok(logs);
    }

    @PostMapping
    public ResponseEntity<SensorLog> addSensorLog(@RequestBody SensorLog sensorLog) {
        SensorLog savedLog = sensorLogService.saveSensorLog(sensorLog);
        return ResponseEntity.ok(savedLog);
    }
}
