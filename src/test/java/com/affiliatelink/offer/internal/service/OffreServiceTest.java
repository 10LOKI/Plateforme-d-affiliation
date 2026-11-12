package com.affiliatelink.offer.internal.service;

import com.affiliatelink.common.InactiveAdvertiserException;
import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.offer.internal.dto.OffreCreateRequest;
import com.affiliatelink.offer.internal.dto.OffreResponse;
import com.affiliatelink.offer.internal.entity.Offre;
import com.affiliatelink.offer.internal.entity.Tag;
import com.affiliatelink.offer.internal.mapper.OffreMapper;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.offer.internal.repository.TagRepository;
import com.affiliatelink.profile.ProfileApi;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OffreServiceTest {
    @Test
    void rejectsCreationWhenAdvertiserDoesNotExist() {
        // Given : l'annonceur 99 n'existe pas.
        var offres = mock(OffreRepository.class);
        var tags = mock(TagRepository.class);
        var profiles = mock(ProfileApi.class);
        var mapper = mock(OffreMapper.class);
        var service = new OffreService(offres, tags, profiles, mapper);
        var request = new OffreCreateRequest(99L, "Offre", "Description",
                "https://example.com", "Sport", Set.of());
        when(profiles.advertiserExists(99L)).thenReturn(false);

        // When / Then : la création est refusée sans sauvegarde.
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ProfileNotFoundException.class);
        verify(profiles).advertiserExists(99L);
        verifyNoMoreInteractions(profiles);
        verifyNoInteractions(offres, tags, mapper);
    }

    @Test
    void createsAnOfferForAnActiveAdvertiser() {
        // Given : l'annonceur existe et son compte est actif.
        var offres = mock(OffreRepository.class);
        var tags = mock(TagRepository.class);
        var profiles = mock(ProfileApi.class);
        var mapper = mock(OffreMapper.class);
        var service = new OffreService(offres, tags, profiles, mapper);
        var request = new OffreCreateRequest(99L, "Offre", "Description",
                "https://example.com", "Sport", Set.of());
        var offre = new Offre();
        var savedOffre = new Offre();
        savedOffre.setId(7L);
        var expected = new OffreResponse(7L, 99L, "Offre", "Description",
                "https://example.com", "Sport", null, Set.of());
        when(profiles.advertiserExists(99L)).thenReturn(true);
        when(profiles.isAdvertiserActive(99L)).thenReturn(true);
        when(mapper.toEntity(request)).thenReturn(offre);
        when(offres.saveAndFlush(offre)).thenReturn(savedOffre);
        when(mapper.toResponse(savedOffre)).thenReturn(expected);

        // When : on demande la création de l'offre.
        var result = service.create(request);

        // Then : le service sauvegarde l'entité et renvoie sa réponse mappée.
        assertThat(result).isSameAs(expected);
        verify(profiles).advertiserExists(99L);
        verify(profiles).isAdvertiserActive(99L);
        verify(mapper).toEntity(request);
        verify(offres).saveAndFlush(offre);
        verify(mapper).toResponse(savedOffre);
        verifyNoInteractions(tags);
    }

    @Test
    void createsOnlyOneNormalizedTagForEquivalentNames() {
        // Given : trois écritures différentes du même tag, encore absent.
        var offres = mock(OffreRepository.class);
        var tags = mock(TagRepository.class);
        var profiles = mock(ProfileApi.class);
        var mapper = mock(OffreMapper.class);
        var service = new OffreService(offres, tags, profiles, mapper);
        var request = new OffreCreateRequest(99L, "Offre", "Description",
                "https://example.com", "Sport", Set.of("  JAVA  ", "java", "Java"));
        var offre = new Offre();
        var tagToSave = new Tag();
        tagToSave.setName("java");
        var savedTag = new Tag();
        savedTag.setId(3L);
        savedTag.setName("java");
        when(profiles.advertiserExists(99L)).thenReturn(true);
        when(profiles.isAdvertiserActive(99L)).thenReturn(true);
        when(mapper.toEntity(request)).thenReturn(offre);
        when(tags.findByName("java")).thenReturn(Optional.empty());
        when(mapper.toTag("java")).thenReturn(tagToSave);
        when(tags.save(tagToSave)).thenReturn(savedTag);
        when(offres.saveAndFlush(offre)).thenReturn(offre);

        // When : on crée l'offre avec ces trois noms.
        service.create(request);

        // Then : un seul tag normalisé est recherché, créé et associé.
        verify(tags).findByName("java");
        verify(mapper).toTag("java");
        verify(tags).save(tagToSave);
        verifyNoMoreInteractions(tags);
        verify(offres).saveAndFlush(offre);
        assertThat(offre.getTags()).containsExactly(savedTag);
        assertThat(savedTag.getOffres()).containsExactly(offre);
    }

    @Test
    void reusesAnExistingTagWithoutCreatingAnother() {
        // Given : le tag normalisé existe déjà dans le repository simulé.
        var offres = mock(OffreRepository.class);
        var tags = mock(TagRepository.class);
        var profiles = mock(ProfileApi.class);
        var mapper = mock(OffreMapper.class);
        var service = new OffreService(offres, tags, profiles, mapper);
        var request = new OffreCreateRequest(99L, "Offre", "Description",
                "https://example.com", "Sport", Set.of("  JaVa "));
        var offre = new Offre();
        var existingTag = new Tag();
        existingTag.setId(3L);
        existingTag.setName("java");
        when(profiles.advertiserExists(99L)).thenReturn(true);
        when(profiles.isAdvertiserActive(99L)).thenReturn(true);
        when(mapper.toEntity(request)).thenReturn(offre);
        when(tags.findByName("java")).thenReturn(Optional.of(existingTag));
        when(offres.saveAndFlush(offre)).thenReturn(offre);

        // When : on crée une offre utilisant ce tag.
        service.create(request);

        // Then : le même tag est associé, sans nouvelle création de tag.
        verify(tags).findByName("java");
        verify(tags, never()).save(any(Tag.class));
        verify(mapper, never()).toTag(anyString());
        verify(offres).saveAndFlush(offre);
        assertThat(offre.getTags()).containsExactly(existingTag);
        assertThat(existingTag.getOffres()).containsExactly(offre);
    }

    @Test
    void rejectsCreationWhenAdvertiserIsInactive() {
        // Given : l'annonceur 99 existe, mais son compte n'est pas actif.
        var offres = mock(OffreRepository.class);
        var tags = mock(TagRepository.class);
        var profiles = mock(ProfileApi.class);
        var mapper = mock(OffreMapper.class);
        var service = new OffreService(offres, tags, profiles, mapper);
        var request = new OffreCreateRequest(99L, "Offre", "Description",
                "https://example.com", "Sport", Set.of());
        when(profiles.advertiserExists(99L)).thenReturn(true);
        when(profiles.isAdvertiserActive(99L)).thenReturn(false);

        // When / Then : le service refuse avant de construire ou sauvegarder l'offre.
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InactiveAdvertiserException.class)
                .hasMessageContaining("99");
        verify(profiles).advertiserExists(99L);
        verify(profiles).isAdvertiserActive(99L);
        verifyNoMoreInteractions(profiles);
        verifyNoInteractions(offres, tags, mapper);
    }
}
