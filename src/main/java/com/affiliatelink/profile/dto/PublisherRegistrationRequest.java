package com.affiliatelink.profile.dto;

import com.affiliatelink.profile.entity.Niche;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublisherRegistrationRequest (

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    String email,
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password,
    @NotBlank(message = "Le prénom est obligatoire")
    String firstName,
    @NotBlank(message = "Le nom est obligatoire")
    String lastName,
    @NotNull(message = "La niche est obligatoire")
    Niche niche,
    @NotBlank(message = "Le pays est obligatoire")
    String country,
    String bio
){}