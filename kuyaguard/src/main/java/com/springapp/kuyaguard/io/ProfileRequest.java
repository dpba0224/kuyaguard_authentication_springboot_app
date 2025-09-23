package com.springapp.kuyaguard.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRequest {

    @NotBlank(message = "Please enter your name!")
    private String name;

    @Email (message = "Please enter a valid e-mail address!")
    @NotNull
    private String email;

    @Size(min = 8, message = "The password should contain at least 8 characters!")
    private String password;
}
