package com.affiliatelink.offer.internal.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record OffreResponse(Long id, Long advertiserId, String title, String description,
                            String landingUrl, String niche, LocalDateTime createdAt,
                            Set<TagResponse> tags) {
}
