package com.affiliatelink.profile.internal.service;

import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProfileServiceTest {
    @Test
    void rejectsAnUnknownProfileWithoutSavingOrPublishing() {
        // Given : le repository ne trouve aucun profil pour cet identifiant.
        var repository = mock(ProfileRepository.class);
        var events = mock(ApplicationEventPublisher.class);
        var service = new ProfileService(repository, events);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then : le service refuse le changement de statut.
        assertThatThrownBy(() -> service.changeStatus(99L, ProfileStatus.SUSPENDED))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(events);
    }
}
