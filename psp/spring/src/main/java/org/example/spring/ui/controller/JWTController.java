package org.example.spring.ui.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.example.spring.ui.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/loginToken")
public class JWTController {

    private JwtService jwtService;

    public JWTController(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    @GetMapping
    public String getToken(@RequestParam String nombre, @RequestParam String password){

        if (nombre.equals("oscar") && password.equals("1234")) {


            // generar token
            // generar token
           return jwtService.generateToken("oscar");
        }
        else
            return "error";
    }



    @GetMapping("/validate")
    public String validateToken(@RequestParam String token){

       jwtService.isTokenValid(token, "oscar");
       System.out.println(jwtService.extractRol(token));
       return jwtService.extractUsername(token);
    }


}
