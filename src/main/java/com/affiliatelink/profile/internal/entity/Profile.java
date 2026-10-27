package com.affiliatelink.profile.internal.entity;

import jakarta.persistence.*;
import com.affiliatelink.common.InvalidProfileStatusException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Profile
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status = ProfileStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ProfileStatus.PENDING;
        }
    }

    public void changeStatus(ProfileStatus newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidProfileStatusException("Transition de statut interdite de " + this.status + " vers " + newStatus);
        }
        this.status = newStatus;
    }

    private boolean isValidTransition(ProfileStatus current, ProfileStatus target) {
        return switch (current) {
            case PENDING, SUSPENDED -> target == ProfileStatus.ACTIVE;
            case ACTIVE -> target == ProfileStatus.SUSPENDED;
        };
    }
}
