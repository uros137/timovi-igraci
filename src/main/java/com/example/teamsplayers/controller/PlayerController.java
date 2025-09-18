package com.example.teamsplayers.controller;

import com.example.teamsplayers.entity.Player;
import com.example.teamsplayers.entity.Team;
import com.example.teamsplayers.repo.PlayerRepository;
import com.example.teamsplayers.repo.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerRepository players;
    private final TeamRepository teams;

    public PlayerController(PlayerRepository players, TeamRepository teams) {
        this.players = players;
        this.teams = teams;
    }

    // ===== LISTA (filter + paging) =====
    @GetMapping
    public Page<Player> all(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String position,
                            @RequestParam(required = false) Long teamId) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        if (teamId != null && position != null && !position.isBlank()) {
            final Long idFinal = teamId;
            Team t = teams.findById(idFinal)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + idFinal));
            return players.findByTeamAndPositionIgnoreCase(t, position, pageable);
        }
        if (teamId != null) {
            final Long idFinal = teamId;
            Team t = teams.findById(idFinal)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + idFinal));
            return players.findByTeam(t, pageable);
        }
        if (position != null && !position.isBlank()) {
            return players.findByPositionIgnoreCase(position, pageable);
        }
        return players.findAll(pageable);
    }

    // ===== DETALJ =====
    @GetMapping("/{id}")
    public Player get(@PathVariable Long id) {
        return players.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrač ne postoji: " + id));
    }

    // ===== KREIRANJE =====
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player create(@RequestParam(required = false) Long teamId,
                         @RequestBody Map<String, Object> body) {

        String fullName = (String) body.get("fullName");
        String position = (String) body.get("position");

        Integer age = null;
        if (body.get("age") != null) {
            Object a = body.get("age");
            age = (a instanceof Number n) ? n.intValue() : Integer.valueOf(a.toString());
        }

        Long bodyTeamId = null;
        if (body.get("teamId") != null) {
            Object tid = body.get("teamId");
            bodyTeamId = (tid instanceof Number n) ? n.longValue() : Long.valueOf(tid.toString());
        }

        Long tid = (teamId != null) ? teamId : bodyTeamId;
        if (tid == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamId je obavezan (query ili body).");
        }

        final Long tidFinal = tid; // <-- final kopija za lambda
        Team t = teams.findById(tidFinal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + tidFinal));

        Player p = new Player();
        p.setFullName(fullName);
        p.setAge(age);
        p.setPosition(position);
        p.setTeam(t);

        return players.save(p);
    }

    // ===== IZMENA =====
    @PutMapping("/{id}")
    public Player update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Player p = players.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrač ne postoji: " + id));

        if (body.containsKey("fullName")) {
            p.setFullName((String) body.get("fullName"));
        }
        if (body.containsKey("age")) {
            Object a = body.get("age");
            p.setAge(a == null ? null : (a instanceof Number n) ? n.intValue() : Integer.valueOf(a.toString()));
        }
        if (body.containsKey("position")) {
            p.setPosition((String) body.get("position"));
        }

        // teamId ili team:{id:...}
        Long newTeamId = null;
        if (body.get("teamId") != null) {
            Object tid = body.get("teamId");
            newTeamId = (tid instanceof Number n) ? n.longValue() : Long.valueOf(tid.toString());
        } else if (body.get("team") instanceof Map<?, ?> teamMap) {
            Object tid = ((Map<?, ?>) teamMap).get("id");
            if (tid != null) newTeamId = (tid instanceof Number n) ? n.longValue() : Long.valueOf(tid.toString());
        }

        if (newTeamId != null) {
            final Long lookupId = newTeamId; // <-- final kopija za lambda
            Team t = teams.findById(lookupId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tim ne postoji: " + lookupId));
            p.setTeam(t);
        }

        return players.save(p);
    }

    // ===== BRISANJE =====
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        players.deleteById(id);
    }
}
