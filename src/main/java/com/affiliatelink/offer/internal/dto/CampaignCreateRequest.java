package com.affiliatelink.offer.internal.dto;

import com.affiliatelink.offer.internal.entity.PayoutModel;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignCreateRequest(
        @NotNull @Positive Long advertiserId,
        @NotBlank @Size(max = 255) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal budget,
        @NotNull PayoutModel payoutModel,
        @NotNull @DecimalMin("0") @Digits(integer = 15, fraction = 4) BigDecimal payoutValue
) {
    @AssertTrue(message = "La date de fin doit être postérieure à la date de début.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Montant fixe > 0 ; pourcentage entre 0 et 100.")
    public boolean isPayoutValid() {
        if (payoutModel == null || payoutValue == null) {
            return true;
        }
        if (payoutModel == PayoutModel.CPA_PERCENT) {
            return payoutValue.signum() >= 0 && payoutValue.compareTo(new BigDecimal("100")) <= 0;
        }
        return payoutValue.signum() > 0;
    }
}
