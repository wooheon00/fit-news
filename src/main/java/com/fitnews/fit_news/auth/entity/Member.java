package com.fitnews.fit_news.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String refreshToken;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String name;
    private String email;
}