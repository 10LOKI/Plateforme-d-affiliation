package com.affiliatelink.profile.service;

import com.affiliatelink.profile.dto.AdvertiserRegistrationRequest;
import com.affiliatelink.profile.dto.AdvertiserRegistrationResponse;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.repository.AdvertiserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdvertiserService {
    private final AdvertiserRepository advertiserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdvertiserService(AdvertiserRepository advertiserRepository, PasswordEncoder passwordEncoder) {
        this.advertiserRepository = advertiserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdvertiserRegistrationResponse create(AdvertiserRegistrationRequest request) {
        Advertiser advertiser = new Advertiser();
        advertiser.setEmail(request.email());
        advertiser.setPassword(passwordEncoder.encode(request.password()));
        advertiser.setCompanyName(request.companyName());
        advertiser.setSector(request.sector());
        advertiser.setWebsite(request.website());
        return AdvertiserRegistrationResponse.from(advertiserRepository.save(advertiser));
    }
}
