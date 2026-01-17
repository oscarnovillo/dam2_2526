package org.example.springsecurity.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Página pública - NO requiere autenticación
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Página pública - NO requiere autenticación
     */
    @GetMapping("/public")
    public String publicPage() {
        return "public";
    }

    /**
     * Página privada - REQUIERE autenticación
     */
    @GetMapping("/private")
    public String privatePage(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities());
        }
        return "private";
    }

    /**
     * Página de login personalizada
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

