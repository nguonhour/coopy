package com.course.controller;

import com.course.entity.Role;
import com.course.entity.User;
import com.course.repository.RoleRepository;
import com.course.repository.UserRepository;
import com.course.config.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @GetMapping({ "/", "/index" })
    public String index() {
        return "index";
    }

    @GetMapping("/signin")
    public String signinPage() {
        return "auth/signin";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    @PostMapping("/api/auth/signin")
    public String signin(@RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/signin?error";
        }

        // manual password check to avoid AuthenticationManager configuration issues
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "redirect:/signin?error";
        }

        String token = jwtService.generateToken(user.getEmail(), List.of(user.getRole().getRoleCode()));
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        String role = user.getRole().getRoleCode();
        if ("ADMIN".equalsIgnoreCase(role))
            return "redirect:/admin/dashboard?adminId=" + user.getId();
        if ("LECTURER".equalsIgnoreCase(role))
            return "redirect:/lecturer/dashboard?lecturerId=" + user.getId();
        if ("STUDENT".equalsIgnoreCase(role))
            return "redirect:/student/dashboard?studentId=" + user.getId();
        return "redirect:/";
    }

    @PostMapping("/api/auth/signup")
    public String signup(@RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        if (userRepository.existsByEmail(email)) {
            return "redirect:/signup?error=exists";
        }

        Role studentRole = roleRepository.findByRoleCode("STUDENT")
                .orElseThrow(() -> new RuntimeException("STUDENT role not found"));
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(studentRole);
        userRepository.save(user);

        // auto login
        String token = jwtService.generateToken(user.getEmail(), List.of(user.getRole().getRoleCode()));
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/student/dashboard?studentId=" + user.getId();
    }

    @GetMapping("/signout")
    public String signout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }

}
