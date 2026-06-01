package com.l2kb.runhome.domain.user.entity;

import com.l2kb.runhome.domain.team.entity.Team;
import com.l2kb.runhome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(length = 4)
    private String password;

    @Column(length = 50)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team favoriteTeam;

    public static User of(String nickname, String password) {
        User user = new User();
        user.nickname = nickname;
        user.password = password;
        return user;
    }

    public void updateFavoriteTeam(Team team) {
        this.favoriteTeam = team;
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public boolean matchesPassword(String input) {
        if (this.password == null) return true;
        return this.password.equals(input);
    }
}
