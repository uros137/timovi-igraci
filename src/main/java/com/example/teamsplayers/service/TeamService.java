package com.example.teamsplayers.service;

import com.example.teamsplayers.entity.Team;
import com.example.teamsplayers.repo.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeamService {
    private final TeamRepository repo;
    public TeamService(TeamRepository repo) { this.repo = repo; }

    public List<Team> all() { return repo.findAll(); }

    public Team get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found: " + id));
    }

    public Team create(Team t) { return repo.save(t); }

    public Team update(Long id, Team t){
        Team db = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found: " + id));
        db.setName(t.getName());
        db.setCity(t.getCity());
        return repo.save(db);
    }

    public void delete(Long id){
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Team not found: " + id);
        }
        repo.deleteById(id);
    }
}
