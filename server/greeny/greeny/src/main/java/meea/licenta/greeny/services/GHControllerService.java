package meea.licenta.greeny.services;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.entities.GHController;
import meea.licenta.greeny.repositories.ComponentRepository;
import meea.licenta.greeny.repositories.GHControllerRepository;
import meea.licenta.greeny.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GHControllerService {

    private final GHControllerRepository controllerRepository;
    private final ComponentRepository componentRepository;
    private final UserRepository userRepository;
    private final ComponentService componentService;

    @Transactional
    public GHController createGHController(GHController controller) {
        GHController savedController = controllerRepository.save(controller);
        componentService.createComponentsForNewController(savedController.getId());
        return savedController;
    }

    // Read operation
    public List<GHController> getAllControllers() {
        return controllerRepository.findAll();
    }

    public Optional<GHController> getControllerById(Integer controllerId) {
        return controllerRepository.findById(controllerId);
    }

    public List<GHController> getControllerByUserId(Integer userId) {
        return controllerRepository.findByUserId(userId);
    }

    public boolean userHasController(Integer userId, Integer controllerId) {
        List<GHController> controllers = getControllerByUserId(userId);
        for (GHController controller : controllers) {
            if (controller.getId().equals(controllerId)) {
                return true;
            }
        }
        return false;
    }

    // Update operation
    public GHController updateController(Integer controllerId, GHController newController) {
        // Ensure the user exists before updating the controller
        if (userRepository.findById(newController.getUserId()).isEmpty()) return null;

        return controllerRepository.findById(controllerId).map(controller -> {
            // Update controller fields
            controller.setName(newController.getName());
            controller.setUserId(newController.getUserId());
            controller.setMinThreshold(newController.getMinThreshold()); // Update minThreshold
            controller.setMaxThreshold(newController.getMaxThreshold()); // Update maxThreshold
            return controllerRepository.save(controller);
        }).orElse(null);
    }

    // Delete operation
    public void deleteController(Integer controllerId) {
        controllerRepository.deleteById(controllerId);
        componentRepository.deleteByControllerId(controllerId);
    }

    public void deleteControllerByUserId(Integer userId) {
        controllerRepository.deleteByUserId(userId);
    }
}
