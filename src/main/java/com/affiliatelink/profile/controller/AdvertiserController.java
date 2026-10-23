package com.affiliatelink.profile.controller;

import com.affiliatelink.profile.dto.AdvertiserRegistrationRequest;
import com.affiliatelink.profile.dto.AdvertiserRegistrationResponse;
import com.affiliatelink.profile.service.AdvertiserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advertisers")
public class AdvertiserController {
    private final AdvertiserService advertiserService;

    public AdvertiserController(AdvertiserService advertiserService) {
        this.advertiserService = advertiserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdvertiserRegistrationResponse create(@Valid @RequestBody AdvertiserRegistrationRequest request) {
        return advertiserService.create(request);
    }

    @GetMapping("/{id}")
    public AdvertiserRegistrationResponse getById(@PathVariable Long id) {
        return advertiserService.getById(id);
    }
}
