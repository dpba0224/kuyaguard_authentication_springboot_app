package com.springapp.kuyaguard.service;

import com.springapp.kuyaguard.io.ProfileRequest;
import com.springapp.kuyaguard.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);

    void sendOtpReset(String email);
    void resetPassword(String email, String otp, String newPassword);

    // methods for verifying email address
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
}
