package com.affiliatelink.offer.internal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record OffreDetailResponse(Long id, Long advertiserId, String title, String description,
                                  String landingUrl, String niche, LocalDateTime createdAt,
                                  Set<TagResponse> tags, List<CampaignResponse> campaigns) {
}
