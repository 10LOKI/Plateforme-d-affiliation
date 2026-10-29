package com.affiliatelink.offer.internal.dto;

import java.util.List;

public record OffrePageResponse(List<OffreResponse> content, int page, int size,
                                long totalElements, int totalPages) {
}
