package meea.licenta.greeny.repositories;

import meea.licenta.greeny.entities.GHController;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GHControllerRepository extends JpaRepository<GHController, Integer> {
    List<GHController> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
