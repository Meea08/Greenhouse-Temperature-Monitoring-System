package meea.licenta.greeny.services;

import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.auth.AuthenticationRequest;
import meea.licenta.greeny.auth.AuthenticationResponse;
import meea.licenta.greeny.auth.RegisterRequest;
import meea.licenta.greeny.config.JwtService;
import meea.licenta.greeny.entities.user.Role;
import meea.licenta.greeny.entities.user.User;
import meea.licenta.greeny.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        // Check if the username or email already exists
        if (repository.findByUsername(request.getUsername()).isPresent() ||
                repository.findByEmail(request.getEmail()).isPresent()) {
            return null;  // Return null if the user already exists
        }
        // Create a new user with the hashed password and default role
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .build();

        // Save the new user in the repository
        repository.save(user);
        // Generate a JWT token for the new user
        var jwtToken = jwtService.generateToken(user, user.getId());
        // Return the authentication response with the JWT token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate the user using the authentication manager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // Retrieve the user details from the repository
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(); // This throws an exception if the user is not found
        // Generate a JWT token for the authenticated user
        var jwtToken = jwtService.generateToken(user, user.getId());
        // Return the authentication response with the JWT token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

}
