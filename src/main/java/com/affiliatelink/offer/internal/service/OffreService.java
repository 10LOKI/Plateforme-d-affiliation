package com.affiliatelink.offer.internal.service;

import com.affiliatelink.common.InactiveAdvertiserException;
import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.offer.internal.dto.OffreCreateRequest;
import com.affiliatelink.offer.internal.dto.OffreResponse;
import com.affiliatelink.offer.internal.mapper.OffreMapper;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.offer.internal.repository.TagRepository;
import com.affiliatelink.profile.ProfileApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OffreService {
    private final OffreRepository offreRepository;
    private final TagRepository tagRepository;
    private final ProfileApi profileApi;
    private final OffreMapper mapper;

    public OffreService(OffreRepository offreRepository, TagRepository tagRepository,
                        ProfileApi profileApi, OffreMapper mapper) {
        this.offreRepository = offreRepository;
        this.tagRepository = tagRepository;
        this.profileApi = profileApi;
        this.mapper = mapper;
    }

    @Transactional
    public OffreResponse create(OffreCreateRequest request) {
        if (!profileApi.advertiserExists(request.advertiserId())) {
            throw new ProfileNotFoundException(request.advertiserId());
        }
        if (!profileApi.isAdvertiserActive(request.advertiserId())) {
            throw new InactiveAdvertiserException(request.advertiserId());
        }
        var offre = mapper.toEntity(request);
        if (request.tags() != null) {
            var names = request.tags().stream()
                    .map(name -> name.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            for (String name : names) {
                var tag = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(mapper.toTag(name)));
                offre.addTag(tag);
            }
        }
        return mapper.toResponse(offreRepository.saveAndFlush(offre));
    }
}
