package com.example.teamsplayers.controller;

import com.example.teamsplayers.entity.Team;
import com.example.teamsplayers.repo.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teams;

    public TeamController(TeamRepository teams) {
        this.teams = teams;
    }

    // LISTA
    @GetMapping
    public List<Team> all() { return teams.findAll(); }

    // DETALJ
    @GetMapping("/{id}")
    public Team get(@PathVariable Long id) {
        return teams.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + id));
    }

    // KREIRANJE (prima JSON: { "name":"Partizan", "city":"Beograd" })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team create(@RequestBody Map<String, Object> body) {
        String name = body.get("name") == null ? null : body.get("name").toString().trim();
        String city = body.get("city") == null ? null : body.get("city").toString().trim();

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naziv tima je obavezan.");
        }
        // korisna validacija za duplikat pre baze
        if (teams.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tim sa tim nazivom već postoji.");
        }

        Team t = new Team();
        t.setName(name);
        t.setCity(city);

        try {
            return teams.save(t);
        } catch (DataIntegrityViolationException e) {
            // fallback ako uniq constraint pukne
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tim sa tim nazivom već postoji.");
        }
    }

    // IZMENA (JSON može sadržati bilo koje polje)
    @PutMapping("/{id}")
    public Team update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Team t = teams.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + id));

        if (body.containsKey("name")) {
            String name = body.get("name") == null ? null : body.get("name").toString().trim();
            if (name == null || name.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naziv tima je obavezan.");
            // ako menja naziv, proveri duplikat (osim ako je ostao isti)
            if (!name.equalsIgnoreCase(t.getName()) && teams.existsByNameIgnoreCase(name))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tim sa tim nazivom već postoji.");
            t.setName(name);
        }
        if (body.containsKey("city")) {
            String city = body.get("city") == null ? null : body.get("city").toString().trim();
            t.setCity(city);
        }

        try {
            return teams.save(t);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tim sa tim nazivom već postoji.");
        }
    }

    // BRISANJE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!teams.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + id);
        teams.deleteById(id);
    }
}
