package meea.licenta.greeny.services;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.entities.user.User;
import meea.licenta.greeny.repositories.GHControllerRepository;
import meea.licenta.greeny.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository         userRepository;
    private final GHControllerRepository controllerRepository;
    private final PasswordEncoder encoder;

    // Create operation
    public User createUser(User user) {
        // Encode the password before saving
        if(userRepository.findByUsername (
                user.getUsername ()).isPresent () ||
                userRepository.findByEmail (user.getEmail ()).isPresent ())
            return null;
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Read operation
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username){
        return userRepository.findByUsername (username);
    }

    // Update operation
    public User updateUser(Integer userId, User newUser) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Update user fields
                    user.setUsername(newUser.getUsername());
                    user.setEmail(newUser.getEmail());
                    user.setFirstname (newUser.getFirstname ());
                    user.setLastname (newUser.getLastname ());

                    //only update the role if it's provided
//                    user.setRole (newUser.getRole ());
                    if(newUser.getRole ()!=null &&
                    !newUser.getRole ().toString ().isEmpty ()){
                        user.setRole (newUser.getRole ());
                    }

                    //only update the password if it's provided
                    if(newUser.getPassword () != null &&
                    !newUser.getPassword ().isEmpty ()){
                        user.setPassword (encoder.encode (newUser.getPassword ()));
                    }
//                    user.setPassword (encoder.encode (newUser.getPassword ()));
                    return userRepository.save(user);
                })
                .orElse(null); // Or throw exception if user not found
    }

    // Delete operation
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
        controllerRepository.deleteByUserId (userId);
    }

    // Change password operation
    public boolean changePassword(Integer userId, String oldPassword, String newPassword){
        Optional<User> userOptional = userRepository.findById (userId);
        if(userOptional.isPresent ()){
            User user = userOptional.get ();
            if(encoder.matches (oldPassword, user.getPassword ())){
                user.setPassword (encoder.encode (newPassword));
                userRepository.save (user);
                return true;
            }
        }
        return false;
    }
}
