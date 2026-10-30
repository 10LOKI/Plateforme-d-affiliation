package com.affiliatelink.offer.internal.dto;

import com.affiliatelink.offer.internal.entity.CampaignStatus;
import com.affiliatelink.offer.internal.entity.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignResponse(Long id, String name, LocalDate startDate, LocalDate endDate,
                                BigDecimal budget, PayoutModel payoutModel,
                                BigDecimal payoutValue, CampaignStatus status) {
}
