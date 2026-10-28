package com.affiliatelink.profile.internal.repository;

import com.affiliatelink.profile.internal.entity.Advertiser;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertiserRepository extends JpaRepository<Advertiser, Long> {
    boolean existsByIdAndStatus(Long id, ProfileStatus status);
}
