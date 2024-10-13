package meea.licenta.greeny.repositories;

import meea.licenta.greeny.entities.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface SensorLogRepository extends JpaRepository<SensorLog, Integer> {
    Optional<SensorLog> findByComponentIdAndTimestamp(Integer componentId, Timestamp timestamp);
    List<SensorLog> findByComponentIdOrderByTimestampAsc(Integer componentId);
}
