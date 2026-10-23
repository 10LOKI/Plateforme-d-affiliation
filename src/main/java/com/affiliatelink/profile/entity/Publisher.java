package com.affiliatelink.profile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_publishers")
@PrimaryKeyJoinColumn(name = "profile_id")
@Getter
@Setter
@NoArgsConstructor
public class Publisher extends Profile {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Niche niche;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private String country;
}