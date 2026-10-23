package com.affiliatelink.common;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(Long id) {
        super("Profil introuvable : " + id);
    }
}
