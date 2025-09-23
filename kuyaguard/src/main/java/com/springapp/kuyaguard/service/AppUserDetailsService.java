package com.springapp.kuyaguard.service;

import com.springapp.kuyaguard.entity.UserEntity;
import com.springapp.kuyaguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

// This is used to represent the UserDetailsService of the web application
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    // UserRepository is injected for this class
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // This is used to check if the e-mail exists in the UserEntity
        UserEntity user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("This email address: " + email + " is not found in our server!"));

        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
