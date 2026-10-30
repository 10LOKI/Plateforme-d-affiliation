package com.affiliatelink.offer.internal.service;

import com.affiliatelink.common.CampaignOwnershipException;
import com.affiliatelink.common.InactiveAdvertiserException;
import com.affiliatelink.common.OffreNotFoundException;
import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.offer.internal.dto.CampaignCreateRequest;
import com.affiliatelink.offer.internal.dto.CampaignResponse;
import com.affiliatelink.offer.internal.mapper.OffreMapper;
import com.affiliatelink.offer.internal.repository.CampaignRepository;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.profile.ProfileApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignService {
    private final OffreRepository offreRepository;
    private final CampaignRepository campaignRepository;
    private final ProfileApi profileApi;
    private final OffreMapper mapper;

    public CampaignService(OffreRepository offreRepository, CampaignRepository campaignRepository,
                           ProfileApi profileApi, OffreMapper mapper) {
        this.offreRepository = offreRepository;
        this.campaignRepository = campaignRepository;
        this.profileApi = profileApi;
        this.mapper = mapper;
    }

    @Transactional
    public CampaignResponse create(Long offreId, CampaignCreateRequest request) {
        var offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new OffreNotFoundException(offreId));
        if (!profileApi.advertiserExists(request.advertiserId())) {
            throw new ProfileNotFoundException(request.advertiserId());
        }
        if (!profileApi.isAdvertiserActive(request.advertiserId())) {
            throw new InactiveAdvertiserException(request.advertiserId());
        }
        if (!offre.getAdvertiserId().equals(request.advertiserId())) {
            throw new CampaignOwnershipException();
        }
        var campaign = mapper.toEntity(request);
        offre.addCampaign(campaign);
        return mapper.toResponse(campaignRepository.saveAndFlush(campaign));
    }
}
