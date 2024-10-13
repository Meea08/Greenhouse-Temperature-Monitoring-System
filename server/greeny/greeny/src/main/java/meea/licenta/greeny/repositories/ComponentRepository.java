package meea.licenta.greeny.repositories;

import meea.licenta.greeny.entities.component.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentRepository extends JpaRepository<Component, Integer> {
    List<Component> findByControllerId(Integer controllerId);
    void deleteByControllerId(Integer controllerId);
    Component findByNameAndControllerId(String name, Integer controllerId);
}
