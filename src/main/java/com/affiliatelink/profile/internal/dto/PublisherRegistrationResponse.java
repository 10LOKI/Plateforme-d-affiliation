package com.affiliatelink.profile.internal.dto;

import com.affiliatelink.profile.internal.entity.Niche;
import com.affiliatelink.profile.internal.entity.Publisher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublisherRegistrationResponse (
        String email,
        String firstName,
        String lastName,
        Niche niche,
        String country,
        String bio
){
    public static PublisherRegistrationResponse from(Publisher p)
    {
        return (new PublisherRegistrationResponse(p.getEmail(),p.getFirstName(), p.getLastName(), p.getNiche(),p.getCountry(),p.getBio()));
    }
}
