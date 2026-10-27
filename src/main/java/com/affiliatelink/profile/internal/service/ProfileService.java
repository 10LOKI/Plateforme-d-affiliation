package com.affiliatelink.profile.internal.service;

import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.profile.ProfileSuspended;
import org.springframework.context.ApplicationEventPublisher;
import com.affiliatelink.profile.internal.entity.Profile;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProfileService(ProfileRepository profileRepository, ApplicationEventPublisher eventPublisher) {
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void changeStatus(Long id, ProfileStatus status) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
        profile.changeStatus(status);
        profileRepository.save(profile);
        if (status == ProfileStatus.SUSPENDED) {
            eventPublisher.publishEvent(new ProfileSuspended(profile.getId()));
        }
    }
}
