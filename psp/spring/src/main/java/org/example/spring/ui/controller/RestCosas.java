package org.example.spring.ui.controller;


import org.example.spring.data.CosaRepository;
import org.example.spring.domain.model.Cosa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/cosas")
public class RestCosas {

    private final CosaRepository cosaRepository;

    public RestCosas(CosaRepository cosaRepository) {
        this.cosaRepository = cosaRepository;
    }

    @GetMapping
    public List<Cosa> listarCosas() {
        return cosaRepository.findAll();
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
    public ResponseEntity<Cosa> crearCosa(@RequestBody Cosa cosa) {
        Cosa nuevaCosa = cosaRepository.save(cosa);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCosa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cosa> actualizarCosa(@PathVariable int id, @RequestBody Cosa cosa) {
        return cosaRepository.update(id, cosa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCosa(@PathVariable int id) {
        if (cosaRepository.delete(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
