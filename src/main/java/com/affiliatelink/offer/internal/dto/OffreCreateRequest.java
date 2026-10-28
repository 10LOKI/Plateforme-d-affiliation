package com.affiliatelink.offer.internal.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

public record OffreCreateRequest(
        @NotNull @Positive Long advertiserId,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @NotBlank @URL @Pattern(regexp = "https?://.+", message = "Une URL HTTP ou HTTPS est requise") String landingUrl,
        @NotBlank @Size(max = 255) String niche,
        Set<@NotBlank @Size(max = 255) String> tags
) {
}
