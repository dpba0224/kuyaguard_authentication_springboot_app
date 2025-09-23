package com.springapp.kuyaguard.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse{
    private String email;

    // This will serve as the JWT token
    private String token;
}
