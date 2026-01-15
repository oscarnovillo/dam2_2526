package org.example.spring.ui.controller;

import org.example.spring.ui.service.JwtService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loginToken")
public class JWTController {

    private final JwtService jwtService;

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
    public String validateToken(@RequestHeader("Authorization") String authHeader){

        // Extraer el token del formato "Bearer <token>"
        String token = authHeader.substring(7); // Eliminar "Bearer "

        jwtService.isTokenValid(token, "oscar");
        System.out.println(jwtService.extractRol(token));
        return jwtService.extractUsername(token);
    }

    @GetMapping("/v")
    public String validateTokenGET(@RequestParam String token){


        jwtService.isTokenValid(token, "oscar");
        System.out.println(jwtService.extractRol(token));
        return jwtService.extractUsername(token);
    }
}
