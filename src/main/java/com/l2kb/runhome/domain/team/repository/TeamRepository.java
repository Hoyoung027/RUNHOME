package com.l2kb.runhome.domain.team.repository;

import com.l2kb.runhome.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByAbbreviation(String abbreviation);
}
