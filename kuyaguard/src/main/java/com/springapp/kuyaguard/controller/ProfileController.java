package com.springapp.kuyaguard.controller;

import com.springapp.kuyaguard.io.ProfileRequest;
import com.springapp.kuyaguard.io.ProfileResponse;
import com.springapp.kuyaguard.service.EmailService;
import com.springapp.kuyaguard.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    // Dependency Injections
    private final ProfileService profileService;
    private final EmailService emailService;

    // Handler method for register
    // Binding the JSON object to the Java Object via the RequestBody
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest profileRequest) {
        ProfileResponse response = profileService.createProfile(profileRequest);

        // Trigger an email for the welcome email
        emailService.sendWelcomeMail(response.getEmail(), response.getName());
        return response;
    }

    // Handler method
    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email){
        return profileService.getProfile(email);
    }
}
