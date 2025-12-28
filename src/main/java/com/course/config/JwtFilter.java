package com.course.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // fallback to cookie
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("JWT".equals(c.getName())) {
                        token = c.getValue();
                    }
                }
            }
        }

        if (token != null) {
            try {
                Claims claims = jwtService.parseClaims(token);
                String username = claims.getSubject();
                log.debug("JwtFilter: token parsed, subject={}", username);
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles", List.class);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                        log.debug("JwtFilter: token valid for user={}", userDetails.getUsername());
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (roles != null) {
                            for (String r : roles) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                            }
                        }
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                                null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("JwtFilter: SecurityContext set for user={} authorities={}",
                                userDetails.getUsername(), authorities);
                    }
                }
            } catch (Exception ex) {
                log.debug("JwtFilter: invalid token - {}", ex.getMessage());
                // invalid token - ignore and continue
            }
        }

        filterChain.doFilter(request, response);
    }
}
