package org.example.spring.data;

import org.example.spring.domain.model.Cosa;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CosaRepository {

    private final List<Cosa> cosas;
    private int nextId = 4;

    public CosaRepository() {
        this.cosas = new ArrayList<>();
        cosas.add(new Cosa(1, 1, "Cosa 1", "Descripción de la cosa 1"));
        cosas.add(new Cosa(2, 1, "Cosa 2", "Descripción de la cosa 2"));
        cosas.add(new Cosa(3, 2, "Cosa 3", "Descripción de la cosa 3"));
    }

    public List<Cosa> findAll() {
        return new ArrayList<>(cosas);
    }

    public Optional<Cosa> findById(int id) {
        return cosas.stream()
                .filter(cosa -> cosa.id() == id)
                .findFirst();
    }

    public Cosa save(Cosa cosa) {
        Cosa nuevaCosa = new Cosa(nextId++, cosa.userId(), cosa.nombre(), cosa.descripcion());
        cosas.add(nuevaCosa);
        return nuevaCosa;
    }

    public Optional<Cosa> update(int id, Cosa cosa) {
        for (int i = 0; i < cosas.size(); i++) {
            if (cosas.get(i).id() == id) {
                Cosa cosaActualizada = new Cosa(id, cosa.userId(), cosa.nombre(), cosa.descripcion());
                cosas.set(i, cosaActualizada);
                return Optional.of(cosaActualizada);
            }
        }
        return Optional.empty();
    }

    public boolean delete(int id,Long userId) {

        return cosas.removeIf(cosa -> cosa.id() == id && cosa.userId()==userId);
    }

    public List<Cosa> findNameLike(String nombre) {

       return cosas.stream().filter(cosa -> cosa.nombre().contains(nombre)).toList();
    }
}
