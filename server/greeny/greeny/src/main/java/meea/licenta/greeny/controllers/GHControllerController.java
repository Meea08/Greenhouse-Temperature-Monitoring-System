package meea.licenta.greeny.controllers;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.entities.GHController;
import meea.licenta.greeny.services.GHControllerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/controller")
@RequiredArgsConstructor
public class GHControllerController {

    private final GHControllerService controllerService;

    // Create
    @PostMapping
    public ResponseEntity<GHController> createController(@RequestBody GHController controller){
        GHController createdController = controllerService.createGHController (controller);
        return createdController != null ?
                new ResponseEntity<> (createdController, HttpStatus.CREATED) :
                new ResponseEntity<> (HttpStatus.BAD_REQUEST);
    }

    // Read
    @GetMapping
    public ResponseEntity<List<GHController>> getAllControllers(){
        List<GHController> controllers = controllerService.getAllControllers ();
        return new ResponseEntity<> (controllers, HttpStatus.OK);
    }

    @GetMapping("/id/{controllerId}")
    public ResponseEntity<GHController> getControllerById(@PathVariable Integer controllerId){
        Optional<GHController> controller = controllerService.getControllerById (controllerId);
        return controller.map (value -> new ResponseEntity<> (value, HttpStatus.OK))
                .orElseGet (()-> new ResponseEntity<> (HttpStatus.NOT_FOUND));
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<List<GHController>> getControllerByUserId(@PathVariable Integer userId){
        List<GHController> controllers = controllerService.getControllerByUserId (userId);
        return new ResponseEntity<> (controllers, HttpStatus.OK);
    }

    // Update
    @PutMapping("/id/{controllerId}")
    public ResponseEntity<GHController> updateController(@PathVariable Integer controllerId, @RequestBody GHController controller){
        GHController updatedController = controllerService.updateController (controllerId, controller);
        return updatedController != null ?
                new ResponseEntity<> (updatedController, HttpStatus.OK):
                new ResponseEntity<> (HttpStatus.BAD_REQUEST);
    }

    // Delete
    @Transactional
    @DeleteMapping("/id/{controllerId}")
    public ResponseEntity<Void> deleteController(@PathVariable Integer controllerId){
        controllerService.deleteController (controllerId);
        return new ResponseEntity<> (HttpStatus.NO_CONTENT);
    }


    @Transactional
    @DeleteMapping("/userId/{userId}")
    public ResponseEntity<Void> deleteUserController(@PathVariable Integer userId){
        controllerService.deleteControllerByUserId (userId);
        return new ResponseEntity<> (HttpStatus.NO_CONTENT);
    }
}
