package com.l2kb.runhome.domain.user.repository;

import com.l2kb.runhome.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickname(String nickname);

    Optional<User> findByNickname(String nickname);

    @EntityGraph(attributePaths = "favoriteTeam")
    Optional<User> findWithTeamById(Long id);
}
