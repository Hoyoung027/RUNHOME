package com.l2kb.runhome.domain.team.entity;

import com.l2kb.runhome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String abbreviation;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 10)
    private String shortName;

    @Column(nullable = false, length = 20)
    private String homeCity;

    @Column(nullable = false, length = 50)
    private String stadiumName;

    @Column(nullable = false, length = 7)
    private String primaryColor;

    @Column(nullable = false, length = 7)
    private String secondaryColor;

    @Column(length = 255)
    private String logoUrl;

    public static Team of(String abbreviation, String name, String shortName, String homeCity,
                          String stadiumName, String primaryColor, String secondaryColor) {
        Team team = new Team();
        team.abbreviation = abbreviation;
        team.name = name;
        team.shortName = shortName;
        team.homeCity = homeCity;
        team.stadiumName = stadiumName;
        team.primaryColor = primaryColor;
        team.secondaryColor = secondaryColor;
        return team;
    }
}
