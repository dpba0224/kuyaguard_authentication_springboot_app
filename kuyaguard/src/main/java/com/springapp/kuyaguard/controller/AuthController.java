package com.springapp.kuyaguard.controller;

import com.springapp.kuyaguard.io.AuthRequest;
import com.springapp.kuyaguard.io.AuthResponse;
import com.springapp.kuyaguard.io.ResetPasswordRequest;
import com.springapp.kuyaguard.service.AppUserDetailsService;
import com.springapp.kuyaguard.service.ProfileService;
import com.springapp.kuyaguard.util.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    // Dependency Injections
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtils jwtUtils;
    private final ProfileService profileService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        try{
            // This is responsible in authenticating the user once logs in
            // Check authenticate method below
            authenticate(authRequest.getEmail(), authRequest.getPassword());

            // Generation of JWT token
            // Once JWT token is generated, this will be sent inside the cookie
            final UserDetails userDetails =  appUserDetailsService.loadUserByUsername(authRequest.getEmail());
            final String jwtToken = jwtUtils.generateToken(userDetails);

            // creation of cookie
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(authRequest.getEmail(), jwtToken));
        }
        catch(BadCredentialsException e){
            // Exception for having bad credentials
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", true);
            errorInfo.put("message", "Email and/or Password is invalid!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
        }
        catch(DisabledException e){
            // Exception if account is disabled
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", true);
            errorInfo.put("message", "Account is disabled!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorInfo);
        }
        catch(Exception e){
            // Generic exception
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", true);
            errorInfo.put("message", "Authentication Failed!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorInfo);
        }
    }

    // authenticate method
    private void authenticate(String email, String password) {

        // Object for Authentication Manager will authenticate the email and password passing into the UserNamePasswordAuthenticationToken
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email){
        // Return true if the email is present
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email){
        try{
            profileService.sendOtpReset(email);
        }
        catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        try{
            profileService.resetPassword(
                    resetPasswordRequest.getEmail(),
                    resetPasswordRequest.getOtp(),
                    resetPasswordRequest.getNewPassword()
            );
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication?.name") String email){
        try{
            profileService.sendOtp(email);
        }
        catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(
            @RequestBody Map<String, Object> request,
            @CurrentSecurityContext(expression = "authentication?.name") String email)
    {
        if(request.get("otp").toString() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have missing details!");
        }

        try{
            profileService.verifyOtp(email, request.get("otp").toString());
        }
        catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("You have logged out successfully!");
    }
}
