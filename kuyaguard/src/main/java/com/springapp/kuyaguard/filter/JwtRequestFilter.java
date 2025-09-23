package com.springapp.kuyaguard.filter;

import com.springapp.kuyaguard.service.AppUserDetailsService;
import com.springapp.kuyaguard.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    // Dependency injections
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtils jwtUtils;

    private static final List<String> PUBLIC_URLS =
            List.of("/login", "/register", "/send-reset-otp", "/reset-password", "/logout");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if(PUBLIC_URLS.contains(path)){
            filterChain.doFilter(request,response);
            return;
        }

        // Initializing values for jwt and email
        String jwt = null;
        String email = null;

        // 1: Check the authorization header and extract the token
        final String authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            // Extract the token
            jwt = authorizationHeader.substring(7);
        }

        // 2: If the authorization is not found in the header, check the cookies, and get the value if there are cookies containing JWT
        if(jwt == null){
            Cookie[] cookies = request.getCookies();
            if(cookies != null){
                // loop thru the cookie
                for(Cookie cookie : cookies){
                    if("jwt".equals(cookie.getName())){
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 3: Validate the token and set the security context
        // This applies if the jwt is not null
        if(jwt != null){
            email = jwtUtils.getEmail(jwt);
            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);

                if(jwtUtils.validateToken(jwt,userDetails)){
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // Pass the request and response to the next filter
        filterChain.doFilter(request, response);
    }
}
