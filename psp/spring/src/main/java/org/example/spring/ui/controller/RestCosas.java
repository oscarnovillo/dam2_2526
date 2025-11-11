package org.example.spring.ui.controller;


import jakarta.servlet.http.HttpSession;
import org.example.spring.data.CosaRepository;
import org.example.spring.domain.model.Cosa;
import org.example.spring.ui.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/cosas")
public class RestCosas {

    private final CosaRepository cosaRepository;
    private final AuthService authService;

    public RestCosas(CosaRepository cosaRepository, AuthService authService) {
        this.cosaRepository = cosaRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Cosa>> listarCosas(HttpSession session) {

        if (authService.isAuthenticated(session)) {
            return ResponseEntity.ok(cosaRepository.findAll());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cosa> obtenerCosa(@PathVariable int id) {
        return cosaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/id")
    public ResponseEntity<Cosa> obtenerCosa2(@RequestParam int id) {
        return cosaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/filtrar")
    public ResponseEntity<List<Cosa>> filtroCosa(@RequestParam String nombre) {

        return ResponseEntity.ok(cosaRepository.findNameLike(nombre));
    }


    @PostMapping
    public ResponseEntity<Cosa> crearCosa(@RequestBody Cosa cosa,HttpSession session) {
        if (authService.isAuthenticated(session)) {
            if (authService.isAdmin(session)) {
                Cosa nuevaCosa = cosaRepository.save(cosa);
                return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCosa);
            }
            else
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cosa> actualizarCosa(@PathVariable int id, @RequestBody Cosa cosa) {
        return cosaRepository.update(id, cosa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCosa(@PathVariable int id, HttpSession session) {
        if (authService.isAuthenticated(session)) {
            Long userId = authService.getUsuarioIdFromSession(session);
            if (cosaRepository.delete(id,userId)) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}
