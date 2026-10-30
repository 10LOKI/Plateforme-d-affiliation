package com.affiliatelink.profile;

import com.affiliatelink.common.InvalidProfileStatusException;
import com.affiliatelink.profile.internal.entity.Advertiser;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.repository.ProfileRepository;
import com.affiliatelink.profile.internal.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileSuspendedTest {
    private final ProfileRepository repository = mock(ProfileRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final ProfileService service = new ProfileService(repository, events);

    @Test
    void publishesTheProfileIdWhenAnActiveProfileIsSuspended() {
        var profile = new Advertiser();
        profile.setId(5L);
        profile.setStatus(ProfileStatus.ACTIVE);
        when(repository.findById(5L)).thenReturn(Optional.of(profile));
        service.changeStatus(5L, ProfileStatus.SUSPENDED);
        assertEquals(ProfileStatus.SUSPENDED, profile.getStatus());
        verify(events).publishEvent(new ProfileSuspended(5L));
    }

    @Test
    void publishesNothingForActivationOrARejectedSuspension() {
        var profile = new Advertiser();
        when(repository.findById(5L)).thenReturn(Optional.of(profile));
        assertThrows(InvalidProfileStatusException.class,
                () -> service.changeStatus(5L, ProfileStatus.SUSPENDED));
        service.changeStatus(5L, ProfileStatus.ACTIVE);
        verifyNoInteractions(events);
    }

    @Test
    void publishesNothingWhenSavingFails() {
        var profile = new Advertiser();
        profile.setStatus(ProfileStatus.ACTIVE);
        when(repository.findById(5L)).thenReturn(Optional.of(profile));
        when(repository.save(profile)).thenThrow(new IllegalStateException("Échec de sauvegarde"));
        assertThrows(IllegalStateException.class,
                () -> service.changeStatus(5L, ProfileStatus.SUSPENDED));
        verifyNoInteractions(events);
    }
}
