package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.LoginRequest;
import com.shivam.urlshortenerservice.dtos.LoginResponse;
import com.shivam.urlshortenerservice.dtos.SignUpRequest;
import com.shivam.urlshortenerservice.dtos.UserDto;
import com.shivam.urlshortenerservice.exceptions.InvalidRequestException;
import com.shivam.urlshortenerservice.models.User;
import com.shivam.urlshortenerservice.services.IAuthService;
import com.shivam.urlshortenerservice.utils.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> register(@RequestBody SignUpRequest request) {
        // validate input
        if (StringUtils.isEmpty(request.getEmail())) throw new InvalidRequestException("Invalid email");
        if (StringUtils.isEmpty(request.getPassword())) throw new InvalidRequestException("Invalid password");
        if (StringUtils.isEmpty(request.getName())) throw new InvalidRequestException("Invalid name");

        User user = authService.signUp(request.getName(),request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // validate input
        if (StringUtils.isEmpty(request.getEmail())) throw new InvalidRequestException("Invalid email");
        if (StringUtils.isEmpty(request.getPassword())) throw new InvalidRequestException("Invalid password");

        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private UserDto from(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }
}

