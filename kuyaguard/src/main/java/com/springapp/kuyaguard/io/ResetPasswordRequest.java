package com.springapp.kuyaguard.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Please input a new password!")
    private String newPassword;

    @NotBlank(message = "Please input the OTP that was sent to you.")
    private String otp;

    @NotBlank(message = "Please enter your e-mail address!")
    private String email;
}
