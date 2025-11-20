package com.djbc.dutyfree.security;

import com.djbc.dutyfree.domain.entity.User;
import com.djbc.dutyfree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Use standard query to get the full User entity
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.debug("User found: {}, active: {}, role: {}", user.getUsername(), user.getActive(), user.getRole());

        if (!user.getActive()) {
            log.error("User is not active: {}", username);
            throw new UsernameNotFoundException("User is not active: " + username);
        }

        log.debug("Creating UserDetails with password hash: {}", user.getPassword().substring(0, 10) + "...");

        return new CustomUserDetails(user.getUsername(), user.getPassword(), user.getRole().toString(), user.getActive());
    }
}