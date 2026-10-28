package com.affiliatelink.offer.internal.repository;

import com.affiliatelink.offer.internal.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
