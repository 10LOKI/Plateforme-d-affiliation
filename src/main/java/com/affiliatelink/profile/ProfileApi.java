package com.affiliatelink.profile;

public interface ProfileApi {
    boolean advertiserExists(Long advertiserId);

    boolean isAdvertiserActive(Long advertiserId);
}
