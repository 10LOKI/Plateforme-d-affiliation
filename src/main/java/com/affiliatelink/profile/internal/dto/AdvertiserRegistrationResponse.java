package com.affiliatelink.profile.internal.dto;

import com.affiliatelink.profile.internal.entity.Advertiser;
import com.affiliatelink.profile.internal.entity.ProfileStatus;

public record AdvertiserRegistrationResponse(
        Long id, String email, String companyName, String sector,
        String website, ProfileStatus status
) {
    public static AdvertiserRegistrationResponse from(Advertiser advertiser) {
        return new AdvertiserRegistrationResponse(
                advertiser.getId(), advertiser.getEmail(), advertiser.getCompanyName(),
                advertiser.getSector(), advertiser.getWebsite(), advertiser.getStatus());
    }
}
