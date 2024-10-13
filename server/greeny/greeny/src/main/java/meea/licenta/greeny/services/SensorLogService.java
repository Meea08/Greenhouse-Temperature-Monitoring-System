package meea.licenta.greeny.services;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.entities.SensorLog;
import meea.licenta.greeny.repositories.SensorLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SensorLogService {
    private final SensorLogRepository sensorLogRepository;

    // Create operation
    public SensorLog saveSensorLog(SensorLog sensorLog) {
        Optional<SensorLog> existingLog = sensorLogRepository.findByComponentIdAndTimestamp(
                sensorLog.getComponent().getControllerId(), sensorLog.getTimestamp());
//        if (existingLog.isPresent()) {
//            // Log already exists
//            return existingLog.get();
//        }
        return sensorLogRepository.save(sensorLog);
    }

    public List<SensorLog> getLogsBySensorId(Integer sensorId) {
        return sensorLogRepository.findByComponentIdOrderByTimestampAsc(sensorId);
    }

}
