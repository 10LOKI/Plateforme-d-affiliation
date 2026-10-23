package com.affiliatelink.profile.controller;


import com.affiliatelink.profile.dto.PublisherRegistrationRequest;
import com.affiliatelink.profile.dto.PublisherRegistrationResponse;
import com.affiliatelink.profile.service.PublisherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController
{
    private final PublisherService pservice;

    public PublisherController(PublisherService pservice) {
        this.pservice = pservice;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherRegistrationResponse create(@Valid @RequestBody PublisherRegistrationRequest pr)
    {
        return pservice.create(pr);
    }

    @GetMapping
    public List<PublisherRegistrationResponse> getAll()
    {
        return pservice.getAll();
    }

    @GetMapping("/{id}")
    public PublisherRegistrationResponse getById(@PathVariable Long id) {
        return pservice.getById(id);
    }
}
