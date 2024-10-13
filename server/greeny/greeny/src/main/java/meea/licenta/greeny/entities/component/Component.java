package meea.licenta.greeny.entities.component;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import meea.licenta.greeny.entities.SensorLog;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "component")
public class Component {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private Integer controllerId;
    @Enumerated(EnumType.STRING)
    private ComponentType type;
    private boolean active;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SensorLog> sensorLogList;

    public void updateWith(Component newComponent) {
        this.active = newComponent.isActive();
    }
}

