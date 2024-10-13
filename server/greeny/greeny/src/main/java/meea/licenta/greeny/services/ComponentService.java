package meea.licenta.greeny.services;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.entities.component.Component;
import meea.licenta.greeny.entities.component.ComponentType;
import meea.licenta.greeny.repositories.ComponentRepository;
import meea.licenta.greeny.repositories.GHControllerRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final GHControllerRepository controllerRepository;

    // Create operation
    public Component createComponent(Component component){
        return controllerRepository.findById (component.getControllerId ()).isPresent () ?
                componentRepository.save (component) :
                null;
    }

    public List<Component> createComponents(List<Component> components) {
        // Ensure all components have the same controllerId and the controller exists
        if (components.isEmpty()) {
            return Collections.emptyList();
        }

        Integer controllerId = components.get(0).getControllerId();
        if (controllerRepository.findById(controllerId).isPresent()) {
            return componentRepository.saveAll(components);
        } else {
            return Collections.emptyList();
        }
    }

    public List<Component> createComponentsForNewController(Integer controllerId) {
        List<Component> components = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            Component component = Component.builder()
                    .name("Temperature Sensor " + i)
                    .controllerId(controllerId)
                    .type(ComponentType.TEMPERATURE_SENSOR)
                    .active(false)  // Initially set to inactive
                    .build();
            components.add(component);
        }
        return componentRepository.saveAll(components);
    }


    // Read operation
    public List<Component> getAllComponents(){
        return componentRepository.findAll ();
    }

    public List<Component> getComponentByControllerId(Integer controllerId){
        return componentRepository.findByControllerId (controllerId);
    }

    public List<Component> getActiveComponentsByControllerId(Integer controllerId){
       List<Component> components = componentRepository.findByControllerId (controllerId);
       List<Component> activeComponents = new ArrayList<>();
       for (Component component : components) {
           if (component.isActive()) {
               activeComponents.add(component);
           }
       }
       return activeComponents;
    }

    public Optional<Component> getComponentById(Integer componentId){
        return componentRepository.findById (componentId);
    }

    // Update operation
    public Component updateComponent(Integer componentId, Component newComponent){
        if(controllerRepository.findById (newComponent.getControllerId ()).isEmpty ()) return null;
        return componentRepository.findById (componentId).map (component -> {
            component.setName (newComponent.getName ());
            component.setType (newComponent.getType ());
            component.setControllerId (newComponent.getControllerId ());
            return componentRepository.save (component);
        }).orElse (null);
    }

    public List<Component> updateComponentsByControllerId(Integer controllerId, List<Component> newComponents) {
        List<Component> existingComponents = componentRepository.findByControllerId(controllerId);

        // Map existing components by ID for easy lookup
        Map<Integer, Component> existingComponentsMap = existingComponents.stream()
                .collect(Collectors.toMap(Component::getId, component -> component));

        // Components to keep or update
        List<Component> componentsToSave = new ArrayList<>();

        // Handle new or updated components
        for (Component newComponent : newComponents) {
            if (newComponent.getId() == null || !existingComponentsMap.containsKey(newComponent.getId())) {
                // New component
                newComponent.setControllerId(controllerId);
                componentsToSave.add(newComponent);
            } else {
                // Update existing component
                Component existingComponent = existingComponentsMap.get(newComponent.getId());
                existingComponent.updateWith(newComponent);  // Assume updateWith is a method to update fields
                componentsToSave.add(existingComponent);
                existingComponentsMap.remove(newComponent.getId()); // Remove from map, it's processed
            }
        }

        // Components to delete (those left in the map are no longer needed)
        List<Component> componentsToDelete = new ArrayList<>(existingComponentsMap.values());
        componentRepository.deleteAll(componentsToDelete);

        // Save the components to keep or update
        return componentRepository.saveAll(componentsToSave);
    }

    // Delete operation
    public void deleteComponent(Integer componentId){
        componentRepository.deleteById (componentId);
    }

    public void deleteComponentByControllerId(Integer controllerId){
        componentRepository.deleteByControllerId (controllerId);
    }
}
