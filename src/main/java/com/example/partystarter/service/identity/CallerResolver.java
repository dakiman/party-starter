package com.example.partystarter.service.identity;

import com.example.partystarter.model.User;
import com.example.partystarter.repo.GuestUserRepository;
import com.example.partystarter.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the current request's caller identity:
 *   1. JWT principal → AuthenticatedUser (preferred when both are present)
 *   2. X-Guest-Token header → Guest
 *   3. Otherwise empty
 *
 * Anonymous-first endpoints (POST /share/{token}/request) handle
 * empty by creating a new GuestUser if the body provides one.
 */
@Component
@AllArgsConstructor
public class CallerResolver {

    public static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

    private final UserRepository userRepository;
    private final GuestUserRepository guestUserRepository;

    public Optional<CallerIdentity> resolve(HttpServletRequest request) {
        Optional<User> authedUser = currentAuthenticatedUser();
        if (authedUser.isPresent()) {
            return Optional.of(new AuthenticatedUser(authedUser.get()));
        }
        String token = request.getHeader(GUEST_TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return guestUserRepository.findByGuestToken(token).map(Guest::new);
    }

    private Optional<User> currentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails ud)) {
            return Optional.empty();
        }
        return userRepository.getByUsername(ud.getUsername());
    }
}
