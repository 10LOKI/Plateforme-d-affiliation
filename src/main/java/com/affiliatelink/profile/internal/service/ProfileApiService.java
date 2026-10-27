package com.affiliatelink.profile.internal.service;

import com.affiliatelink.profile.ProfileApi;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.repository.AdvertiserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileApiService implements ProfileApi {
    private final AdvertiserRepository advertiserRepository;

    public ProfileApiService(AdvertiserRepository advertiserRepository) {
        this.advertiserRepository = advertiserRepository;
    }

    @Override
    public boolean advertiserExists(Long advertiserId) {
        return advertiserRepository.existsById(advertiserId);
    }

    @Override
    public boolean isAdvertiserActive(Long advertiserId) {
        return advertiserRepository.existsByIdAndStatus(advertiserId, ProfileStatus.ACTIVE);
    }
}
