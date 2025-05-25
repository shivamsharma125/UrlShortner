package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.exceptions.InvalidCredentialsException;
import com.shivam.urlshortenerservice.exceptions.RoleNotFoundException;
import com.shivam.urlshortenerservice.exceptions.UserAlreadyExistsException;
import com.shivam.urlshortenerservice.models.Role;
import com.shivam.urlshortenerservice.models.User;
import com.shivam.urlshortenerservice.repositories.RoleRepository;
import com.shivam.urlshortenerservice.repositories.UserRepository;
import com.shivam.urlshortenerservice.security.services.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public AuthService(UserRepository userRepository, JwtService jwtService,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
    }

    @Override
    public User signUp(String name, String email, String password, Set<String> rolesStr) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already registered.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        Set<Role> roles = rolesStr.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RoleNotFoundException("role '" + roleName + "' does not exist")))
                .collect(Collectors.toSet());

        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtService.generateToken(email);
    }
}
