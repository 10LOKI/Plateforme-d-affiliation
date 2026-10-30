package com.affiliatelink.offer.internal.controller;

import com.affiliatelink.offer.internal.dto.CampaignCreateRequest;
import com.affiliatelink.offer.internal.dto.CampaignResponse;
import com.affiliatelink.offer.internal.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CampaignController {
    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping("/api/offres/{id}/campaigns")
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@PathVariable Long id, @Valid @RequestBody CampaignCreateRequest request) {
        return campaignService.create(id, request);
    }
}
