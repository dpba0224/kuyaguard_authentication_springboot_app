package com.springapp.kuyaguard.service;

import com.springapp.kuyaguard.entity.UserEntity;
import com.springapp.kuyaguard.io.ProfileRequest;
import com.springapp.kuyaguard.io.ProfileResponse;
import com.springapp.kuyaguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);

        // validation if the email already exists
        if(!userRepository.existsByEmail(request.getEmail())){
            newProfile = userRepository.save(newProfile);

            // convert UserEntity into ProfileResponse
            return convertToProfileResponse(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendOtpReset(String email) {
        // Get the existing profile
        UserEntity entity =  userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in this e-mail address: " + email));

        // Generate the OTP for the entered e-mail address
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,999999));

        // Expiry Time after 10 minutes > convert to ms
        long expirationTime = System.currentTimeMillis() + (10 * 60 * 1000);

        // Update the profile entity
        entity.setResetOtp(otp);
        entity.setResetOtpExpireAt(expirationTime);

        // Update to the database
        userRepository.save(entity);

        try{
            // Send the reset OTP to the email
            emailService.sendEmailOtp(entity.getEmail(), otp);
        }
        catch(Exception e){
            throw new RuntimeException("Sorry we cannot send the email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // First validation: Missing or wrong OTP
        if(user.getResetOtp() == null || !user.getResetOtp().equals(otp)){
            throw new RuntimeException("This is an invalid OTP!");
        }
        // Second validation: Expired OTP
        if(user.getResetOtpExpireAt() < System.currentTimeMillis()){
            throw new RuntimeException("OTP is already expired!");
        }

        // Setting the password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpireAt(0L);

        // Save the user to the database
        userRepository.save(user);
    }

    // Builder methods for e-mail address verification
    @Override
    public void sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for this e-mail address: " + email));

        if(user.getIsAccountVerified() != null && user.getIsAccountVerified()){
            return;
        }

        // Generate the OTP
        // Generate the OTP for the entered e-mail address
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,999999));

        // Expiry Time after 24 hours > convert to ms
        long expirationTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        // Update the user entity
        user.setVerifyOtp(otp);
        user.setVerifyOtpExpireAt(expirationTime);

        // Save the user's updates to the db
        userRepository.save(user);

        try{
            emailService.sendEmailOtp(user.getEmail(), otp);
        }
        catch(Exception e){
            throw new RuntimeException("Sorry we cannot send the email.");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User is not found for this e-mail address: " + email));

        if(user.getVerifyOtp() == null || !user.getVerifyOtp().equals(otp)){
            throw new RuntimeException("This is an invalid OTP!");
        }

        if(user.getVerifyOtpExpireAt() < System.currentTimeMillis()){
            throw new RuntimeException("OTP is already expired!");
        }

        user.setIsAccountVerified(true);
        user.setVerifyOtp(null);
        user.setVerifyOtpExpireAt(0L);

        userRepository.save(user);
    }

    /*
        The builder method, or more broadly, the Builder design pattern, in Spring Boot is a creational design pattern used to construct complex objects step-by-step. It's particularly useful when an object has many potential fields, some optional, and you want to avoid a proliferation of constructors or cumbersome setter chains.
    */

    // Builder methods for ProfileResponse and UserEntity
    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }
}
