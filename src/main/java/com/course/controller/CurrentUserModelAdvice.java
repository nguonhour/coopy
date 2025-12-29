package com.course.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import com.course.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserModelAdvice {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addCurrentUserAttributes(Model model, Principal principal) {
        if (principal == null)
            return;
        userRepository.findByEmail(principal.getName()).ifPresent(u -> {
            model.addAttribute("userId", u.getId());
            if (u.getRole() != null) {
                // expose role code used in templates (e.g. "ADMIN", "LECTURER", "STUDENT")
                model.addAttribute("role", u.getRole().getRoleCode());
            } else {
                model.addAttribute("role", "");
            }
        });
    }
}
