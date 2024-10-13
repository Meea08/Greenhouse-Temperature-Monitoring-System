package meea.licenta.greeny.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import meea.licenta.greeny.services.GHControllerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final GHControllerService ghControllerService;

    private static final String USER_ID_PATH = "/api/users/id/\\d+";
    private static final String USERNAME_PATH = "/api/users/username/[a-zA-Z0-9._-]+";
    private static final String CHANGE_PASSWORD_PATH = "/api/users/change-password/\\d+";
    private static final String CONTROLLER_ID_PATH = "/api/controller/id/\\d+";
    private static final String CONTROLLER_USERID_PATH = "/api/controller/userId/\\d+";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isUserIdPath(path) || isControllerUserIdPath(path)) {
            handleUserIdRequest(path, request, response, filterChain);
        } else if (isUsernamePath(path)) {
            handleUsernameRequest(path, request, response, filterChain);
        } else if(isChangePasswordPath(path)){
            handleChangePasswordRequest(path, request, response, filterChain);
        } else if(isControllerIdPath(path)){
            handleControllerIdRequest(path, request, response, filterChain);
        }
        else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isUserIdPath(String path) {
        return path.matches(USER_ID_PATH);
    }

    private boolean isUsernamePath(String path) {
        return path.matches(USERNAME_PATH);
    }

    private boolean isChangePasswordPath(String path) {
        return path.matches(CHANGE_PASSWORD_PATH);
    }

    private boolean isControllerIdPath(String path) {
        return path.matches(CONTROLLER_ID_PATH);
    }

    private boolean isControllerUserIdPath(String path) {
        return path.matches(CONTROLLER_USERID_PATH);
    }

    private void handleUserIdRequest(String path, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        Integer requestedUserId = extractUserIdFromPath(path);
        Integer currentUserId = getCurrentUserIdFromToken(request);

        if (!isAuthorizedForUserId(requestedUserId, currentUserId)) {
            denyAccess(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleUsernameRequest(String path, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String requestedUsername = extractUsernameFromPath(path);
        String currentUsername = getCurrentUserUsername(request);

        if (!isAuthorizedForUsername(requestedUsername, currentUsername)) {
            denyAccess(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleChangePasswordRequest(String path, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        
        Integer requestedUserId = extractUserIdFromPath(path);
        Integer currentUserId = getCurrentUserIdFromToken(request);

        if(!isAuthorizedForPasswordChange(requestedUserId, currentUserId)){
            denyAccess(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleControllerIdRequest(String path, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        Integer requestedControllerId = extractControllerIdFromPath(path);
        Integer currentUserId = getCurrentUserIdFromToken(request);

        if(!isAuthorizedForControllerId(requestedControllerId, currentUserId)){
            denyAccess(response);
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void denyAccess(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Access Denied");
        response.getWriter().flush();
        response.getWriter().close();
        return;
    }

    private Integer extractUserIdFromPath(String path) {
        String userIdParam = path.substring(path.lastIndexOf('/') + 1);
        return Integer.parseInt(userIdParam);
    }

    private Integer extractControllerIdFromPath(String path) {
        String controllerIdParam = path.substring(path.lastIndexOf('/') + 1);
        return Integer.parseInt(controllerIdParam);
    }

    private String extractUsernameFromPath(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private boolean isAuthorizedForUserId(Integer requestedUserId, Integer currentUserId) {
        return isAdmin() || requestedUserId.equals(currentUserId);
    }

    private boolean isAuthorizedForUsername(String requestedUsername, String currentUsername) {
        return
                isAdmin() ||
                requestedUsername.equals(currentUsername);
    }

    private boolean isAuthorizedForPasswordChange(Integer requestedUserId, Integer currentUserId) {
        return requestedUserId.equals(currentUserId);
    }

    private boolean isAuthorizedForControllerId(Integer requestedControllerId, Integer currentUserId) {
        return isAdmin() || ghControllerService.userHasController(currentUserId, requestedControllerId);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }

    private Integer getCurrentUserIdFromToken(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        return jwtService.extractUserId(jwt);
    }

    private String getCurrentUserUsername(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        return jwtService.extractUsername(jwt);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalStateException("Authorization header missing or invalid");
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }
}
