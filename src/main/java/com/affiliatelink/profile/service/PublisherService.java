package com.affiliatelink.profile.service;

import com.affiliatelink.common.ProfileNotFoundException;


import com.affiliatelink.profile.dto.PublisherRegistrationRequest;
import com.affiliatelink.profile.dto.PublisherRegistrationResponse;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.repository.PublisherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PublisherService
{
    private final PublisherRepository pr;
    private final PasswordEncoder passwordEncoder;

    public PublisherService(PublisherRepository pr, PasswordEncoder passwordEncoder) {
        this.pr = pr;
        this.passwordEncoder = passwordEncoder;
    }

    public PublisherRegistrationResponse create(PublisherRegistrationRequest request)
    {
        Publisher publisher = new Publisher();
        publisher.setBio(request.bio());
        publisher.setCountry(request.country());
        publisher.setFirstName(request.firstName());
        publisher.setLastName(request.lastName());
        publisher.setEmail(request.email());
        publisher.setNiche(request.niche());
        publisher.setPassword(passwordEncoder.encode(request.password()));
        return PublisherRegistrationResponse.from(pr.save(publisher));
    }

    public List<PublisherRegistrationResponse> getAll()
    {
        return pr.findAll().stream().map(PublisherRegistrationResponse::from).toList();
    }

    public PublisherRegistrationResponse getById(Long id) {
        Publisher profile = pr.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
        return PublisherRegistrationResponse.from(profile);
    }
}
