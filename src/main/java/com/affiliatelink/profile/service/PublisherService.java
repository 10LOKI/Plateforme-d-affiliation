package com.affiliatelink.profile.service;


import com.affiliatelink.profile.dto.PublisherRegistrationRequest;
import com.affiliatelink.profile.dto.PublisherRegistrationResponse;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PublisherService
{
    private final PublisherRepository pr;

    public PublisherService(PublisherRepository pr) {
        this.pr = pr;
    }

    public PublisherRegistrationResponse create(PublisherRegistrationRequest request)
    {
        Publisher publisher = new Publisher();
        publisher.setBio(request.bio());
        publisher.setCountry(request.country());
        publisher.setFirstName(request.firstName());
        publisher.setLastName(request.lastName());
        return PublisherRegistrationResponse.from(pr.save(publisher));
    }

    public List<PublisherRegistrationResponse> getAll()
    {
        return pr.findAll().stream().map(PublisherRegistrationResponse::from).toList();
    }
}
