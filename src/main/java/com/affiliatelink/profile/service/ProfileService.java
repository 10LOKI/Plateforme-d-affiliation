package com.affiliatelink.profile.service;

import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.profile.entity.Profile;
import com.affiliatelink.profile.entity.ProfileStatus;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public void changeStatus(Long id, ProfileStatus status) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
        profile.changeStatus(status);
        profileRepository.save(profile);
    }
}
