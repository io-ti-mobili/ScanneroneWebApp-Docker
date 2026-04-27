package com.scannerone.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String deviceToken;

    @Column(unique = true, nullable = false, length = 32)
    private String username;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int totalUploaded = 0;

    @Column(nullable = false)
    private int uniqueDiscovered = 0;

    @Column(nullable = false)
    private int citiesCovered = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastUploadAt = LocalDateTime.now();

    protected User() {
    }

    public User(String deviceToken, String username) {
        this.deviceToken = deviceToken;
        this.username = username;
    }

}