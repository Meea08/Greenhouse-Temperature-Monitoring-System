package meea.licenta.greeny.controllers;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.entities.component.Component;
import meea.licenta.greeny.services.ComponentService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/component")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    // Create
    @PostMapping
    public ResponseEntity<Component> createComponent(@RequestBody Component component) {
        Component createdComponent = componentService.createComponent(component);
        return createdComponent != null ?
                new ResponseEntity<>(createdComponent, HttpStatus.CREATED) :
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Component>> createComponents(@RequestBody List<Component> components) {
        List<Component> createdComponents = componentService.createComponents(components);
        return !createdComponents.isEmpty() ?
                new ResponseEntity<>(createdComponents, HttpStatus.CREATED) :
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Read
    @GetMapping
    public ResponseEntity<List<Component>> getAllComponents(){
        List<Component> components = componentService.getAllComponents();
        return new ResponseEntity<>(components, HttpStatus.OK);
    }

    @GetMapping("/id/{componentId}")
    public ResponseEntity<Component> getComponentById(@PathVariable Integer componentId){
        return componentService.getComponentById(componentId)
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/controller/{controllerId}")
    public ResponseEntity<List<Component>> getComponentsByControllerId(@PathVariable Integer controllerId){
        List<Component> components = componentService.getComponentByControllerId (controllerId);
        return new ResponseEntity<>(components, HttpStatus.OK);
    }

    @GetMapping("/active/controller/{controllerId}")
    public ResponseEntity<List<Component>> getActiveComponentsByControllerId(@PathVariable Integer controllerId){
        List<Component> components = componentService.getActiveComponentsByControllerId (controllerId);
        return new ResponseEntity<>(components, HttpStatus.OK);
    }

    // Update
    @PutMapping("/id/{componentId}")
    public ResponseEntity<Component> updateComponent(@PathVariable Integer componentId, @RequestBody Component component){
        Component updatedComponent = componentService.updateComponent(componentId, component);
        return updatedComponent != null ?
                new ResponseEntity<>(updatedComponent, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/controllerId/{controllerId}")
    public ResponseEntity<List<Component>> updateComponentsByControllerId(
            @PathVariable Integer controllerId, @RequestBody List<Component> components) {

        List<Component> updatedComponents = componentService.updateComponentsByControllerId(controllerId, components);
        return new ResponseEntity<>(updatedComponents, HttpStatus.OK);
    }

    // Delete
    @DeleteMapping("/id/{componentId}")
    public ResponseEntity<Response> deleteComponent(@PathVariable Integer componentId){
        componentService.deleteComponent(componentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/controller/{controllerId}")
    public ResponseEntity<Response> deleteComponentsByControllerId(@PathVariable Integer controllerId){
        componentService.deleteComponentByControllerId (controllerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
