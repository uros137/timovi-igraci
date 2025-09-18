package com.example.teamsplayers.repo;

import com.example.teamsplayers.entity.Player;
import com.example.teamsplayers.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Page<Player> findByPositionIgnoreCase(String position, Pageable pageable);
    Page<Player> findByTeam(Team team, Pageable pageable);
    Page<Player> findByTeamAndPositionIgnoreCase(Team team, String position, Pageable pageable);
}
