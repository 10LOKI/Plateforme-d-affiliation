package com.affiliatelink.profile.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdvertiserRegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String companyName,
        @NotBlank String sector,
        @NotBlank String website
) {}
