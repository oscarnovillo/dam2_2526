package org.example.spring.ui.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hash")
public class PasswordHashController {

    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private final Pbkdf2PasswordEncoder pbkdf2Encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @GetMapping
    public String hashPassword(
            @RequestParam(required = false) String password,
            @RequestParam(defaultValue = "bcrypt") String algorithm,
            Model model) {

        if (password != null && !password.isEmpty()) {
            String hash;
            String algorithmUsed;

            switch (algorithm.toLowerCase()) {
                case "pbkdf2":
                    hash = pbkdf2Encoder.encode(password);
                    algorithmUsed = "PBKDF2";
                    break;
                case "bcrypt":
                default:
                    hash = bcryptEncoder.encode(password);
                    algorithmUsed = "BCrypt";
                    break;
            }

            model.addAttribute("password", password);
            model.addAttribute("hash", hash);
            model.addAttribute("algorithm", algorithmUsed);
            model.addAttribute("hashLength", hash.length());
        }

        return "password-hash";
    }
}

