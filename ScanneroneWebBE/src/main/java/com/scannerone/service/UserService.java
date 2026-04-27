package com.scannerone.service;


import com.scannerone.entity.User;
import com.scannerone.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Recupera l'utente dal deviceToken.
     * Se non esiste ancora lo crea (primo accesso dell'app).
     * Se viene passato uno username e l'utente non ne ha uno, lo assegna.
     */
    @Transactional
    public User getOrCreate(String deviceToken, String requestedUsername) {
        return userRepository.findByDeviceToken(deviceToken).map(user -> {
            if (requestedUsername != null && !requestedUsername.isBlank()) {
                String sanitized = requestedUsername.trim();
                if (!sanitized.equals(user.getUsername())) {
                    if (!userRepository.existsByUsername(sanitized)) {
                        user.setUsername(sanitized);
                    }
                }
            }
            return user;
        }).orElseGet(() -> {
            String username = resolveUsername(requestedUsername, deviceToken);
            String password = UUID.randomUUID().toString();
            return userRepository.save(new User(deviceToken, password, username));
        });
    }

    @Transactional
    public User registerUser() {
        String uuid = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        String username = resolveUsername(null, uuid);
        return userRepository.save(new User(uuid, password, username));
    }

    @Transactional(readOnly = true)
    public User authenticate(String deviceToken, String password) {
        return userRepository.findByDeviceTokenAndPassword(deviceToken, password)
                .orElseThrow(() -> new SecurityException("Credenziali non valide"));
    }

    @Transactional
    public User updateUsername(long userId, String newUsername) {
        User user = findById(userId);
        if (userRepository.existsByUsername(newUsername.trim())) {
            throw new IllegalArgumentException("Username '" + newUsername + "' già in uso");
        }
        user.setUsername(newUsername.trim());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchFieldError("Utente " + id + " non trovato"));
    }

    @Transactional(readOnly = true)
    public User findByToken(String deviceToken) {
        return userRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new IllegalArgumentException("Token non registrato"));
    }

    // --- Helpers privati ---

    private void tryAssignUsername(User user, String requested) {
        String sanitized = requested.trim();
        if (sanitized.isBlank()) return;
        if (userRepository.existsByUsername(sanitized)) return; // già preso, non tocchiamo
        user.setUsername(sanitized);
        // nessuna save esplicita: dirty-checking JPA dentro @Transactional
    }

    private String resolveUsername(String requested, String deviceToken) {
        if (requested != null && !requested.isBlank()) {
            String sanitized = requested.trim();
            if (!userRepository.existsByUsername(sanitized)) return sanitized;
        }
        // Fallback: Scanner# + prime 6 char del token (senza trattini)
        String shortToken = deviceToken.replace("-", "").substring(0, 6);
        return "Scanner#" + shortToken;
    }
}
