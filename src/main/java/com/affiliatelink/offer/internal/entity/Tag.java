package com.affiliatelink.offer.internal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "offer_tags")
@Getter
@Setter
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.NONE)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @Setter(AccessLevel.NONE)
    private Set<Offre> offres = new HashSet<>();

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du tag est obligatoire.");
        }
        this.name = name.trim().toLowerCase(Locale.ROOT);
    }
}
