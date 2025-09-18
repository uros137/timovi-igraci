package com.example.teamsplayers.service;

import com.example.teamsplayers.entity.Player;
import com.example.teamsplayers.entity.Team;
import com.example.teamsplayers.repo.PlayerRepository;
import com.example.teamsplayers.repo.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class PlayerService {
    private final PlayerRepository players;
    private final TeamRepository teams;

    public PlayerService(PlayerRepository players, TeamRepository teams) {
        this.players = players;
        this.teams = teams;
    }

    // Pagination + filteri
    public Page<Player> all(Pageable pageable, String position, Long teamId){
        if (teamId != null && position != null) {
            Team t = teams.findById(teamId)
                    .orElseThrow(() -> new NoSuchElementException("Team not found: " + teamId));
            return players.findByTeamAndPositionIgnoreCase(t, position, pageable);
        } else if (teamId != null) {
            Team t = teams.findById(teamId)
                    .orElseThrow(() -> new NoSuchElementException("Team not found: " + teamId));
            return players.findByTeam(t, pageable);
        } else if (position != null) {
            return players.findByPositionIgnoreCase(position, pageable);
        }
        return players.findAll(pageable);
    }

    public Player get(Long id){
        return players.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));
    }

    public Player create(Long teamId, Player p){
        Team t = teams.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found: " + teamId));
        p.setTeam(t);
        return players.save(p);
    }

    public Player update(Long id, Player p){
        Player db = players.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + id));
        db.setFullName(p.getFullName());
        db.setAge(p.getAge());
        db.setPosition(p.getPosition());
        if (p.getTeam() != null) db.setTeam(p.getTeam());
        return players.save(db);
    }

    public void move(Long playerId, Long teamId){
        Player pl = players.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + playerId));
        Team t = teams.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found: " + teamId));
        pl.setTeam(t);
        players.save(pl);
    }

    public void delete(Long id){
        if (!players.existsById(id)) {
            throw new NoSuchElementException("Player not found: " + id);
        }
        players.deleteById(id);
    }
}
