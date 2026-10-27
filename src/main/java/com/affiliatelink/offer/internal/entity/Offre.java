package com.affiliatelink.offer.internal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "offer_offres")
@Getter
@Setter
@NoArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long advertiserId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String landingUrl;

    @Column(nullable = false)
    private String niche;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "offre")
    @Setter(AccessLevel.NONE)
    private List<Campaign> campaigns = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "offer_offre_tags",
            joinColumns = @JoinColumn(name = "offre_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Setter(AccessLevel.NONE)
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addCampaign(Campaign campaign) {
        campaign.setOffre(this);
        campaigns.add(campaign);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getOffres().add(this);
    }
}
