package com.affiliatelink.profile.controller;


import com.affiliatelink.profile.dto.PublisherRegistrationRequest;
import com.affiliatelink.profile.dto.PublisherRegistrationResponse;
import com.affiliatelink.profile.service.PublisherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publisher")
public class PublisherController
{
    private final PublisherService pservice;

    public PublisherController(PublisherService pservice) {
        this.pservice = pservice;
    }

    @PostMapping
    public PublisherRegistrationResponse create(@Valid @RequestBody PublisherRegistrationRequest pr)
    {
        return pservice.create(pr);
    }

    @GetMapping
    public List<PublisherRegistrationResponse> getAll()
    {
        return pservice.getAll();
    }
}
