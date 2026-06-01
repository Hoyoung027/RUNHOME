package com.l2kb.runhome.domain.team.repository;

import com.l2kb.runhome.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByAbbreviation(String abbreviation);

    Optional<Team> findByAbbreviation(String abbreviation);
}
