package com.affiliatelink.offer.internal.service;

import com.affiliatelink.common.InactiveAdvertiserException;
import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.common.OffreNotFoundException;
import com.affiliatelink.common.InvalidOfferSearchException;
import com.affiliatelink.offer.internal.dto.OffrePageResponse;
import com.affiliatelink.offer.internal.dto.OffreDetailResponse;
import com.affiliatelink.offer.internal.repository.OffreSpecifications;
import com.affiliatelink.offer.internal.dto.OffreCreateRequest;
import com.affiliatelink.offer.internal.dto.OffreUpdateRequest;
import com.affiliatelink.offer.internal.dto.OffreResponse;
import com.affiliatelink.offer.internal.entity.Offre;
import com.affiliatelink.offer.internal.mapper.OffreMapper;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.offer.internal.repository.TagRepository;
import com.affiliatelink.profile.ProfileApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Locale;
import java.util.Set;
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
        addTags(offre, request.tags());
        return mapper.toResponse(offreRepository.saveAndFlush(offre));
    }

    @Transactional
    public OffreResponse update(Long id, OffreUpdateRequest request) {
        var offre = offreRepository.findById(id)
                .orElseThrow(() -> new OffreNotFoundException(id));
        mapper.update(request, offre);
        offre.clearTags();
        addTags(offre, request.tags());
        return mapper.toResponse(offreRepository.saveAndFlush(offre));
    }

    @Transactional(readOnly = true)
    public OffreDetailResponse getById(Long id) {
        var offre = offreRepository.findById(id)
                .orElseThrow(() -> new OffreNotFoundException(id));
        return mapper.toDetailResponse(offre);
    }

    @Transactional(readOnly = true)
    public OffrePageResponse search(String niche, Long advertiserId, String tag, String keyword,
                                    int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidOfferSearchException("page doit être >= 0 et size entre 1 et 100.");
        }
        if (advertiserId != null && advertiserId <= 0) {
            throw new InvalidOfferSearchException("advertiserId doit être positif.");
        }
        Sort ordering = parseSort(sort);
        var result = offreRepository.findAll(
                OffreSpecifications.withFilters(niche, advertiserId, tag, keyword),
                PageRequest.of(page, size, ordering));
        return mapper.toPageResponse(result.map(mapper::toResponse));
    }

    private Sort parseSort(String value) {
        String[] parts = value.split(",", -1);
        var allowed = Set.of("id", "title", "createdAt", "niche", "advertiserId");
        if (parts.length < 1 || parts.length > 2 || !allowed.contains(parts[0].trim())) {
            throw new InvalidOfferSearchException("Tri autorisé : id, title, createdAt, niche ou advertiserId.");
        }
        String direction = parts.length == 2 ? parts[1].trim() : "asc";
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new InvalidOfferSearchException("Direction de tri attendue : asc ou desc.");
        }
        String property = parts[0].trim();
        Sort result = Sort.by(Sort.Direction.fromString(direction), property);
        return property.equals("id") ? result : result.and(Sort.by("id"));
    }

    private void addTags(Offre offre, Set<String> tags) {
        if (tags != null) {
            var names = tags.stream()
                    .map(name -> name.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            for (String name : names) {
                var tag = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(mapper.toTag(name)));
                offre.addTag(tag);
            }
        }
    }
}
