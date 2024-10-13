package meea.licenta.greeny.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import meea.licenta.greeny.entities.component.Component;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
//@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "sensor_log", uniqueConstraints = @UniqueConstraint(columnNames = {"sensor_id", "timestamp"}))
public class SensorLog {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "sensor_id", referencedColumnName = "id")
    private Component component;
    private Timestamp timestamp;
    private Float value;

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass (this) != Hibernate.getClass (o)) return false;
        SensorLog sensorLog = (SensorLog) o;
        return id != null && Objects.equals (id, sensorLog.id);
    }

    @Override
    public int hashCode () {
        return getClass ().hashCode ();
    }
}
