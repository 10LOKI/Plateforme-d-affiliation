package com.affiliatelink.profile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_advertisers")
@PrimaryKeyJoinColumn(name = "profile_id")
@Getter
@Setter
@NoArgsConstructor
public class Advertiser extends Profile {

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String website;
}