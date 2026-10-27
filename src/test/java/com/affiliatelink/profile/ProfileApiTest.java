package com.affiliatelink.profile;

import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.repository.AdvertiserRepository;
import com.affiliatelink.profile.internal.service.ProfileApiService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileApiTest {
    @Test
    void distinguishesMissingInactiveAndActiveAdvertisers() {
        var repository = mock(AdvertiserRepository.class);
        ProfileApi api = new ProfileApiService(repository);
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(2L)).thenReturn(true);
        when(repository.existsByIdAndStatus(2L, ProfileStatus.ACTIVE)).thenReturn(true);
        assertFalse(api.advertiserExists(99L));
        assertFalse(api.isAdvertiserActive(99L));
        assertTrue(api.advertiserExists(1L));
        assertFalse(api.isAdvertiserActive(1L));
        assertTrue(api.advertiserExists(2L));
        assertTrue(api.isAdvertiserActive(2L));
    }
}
