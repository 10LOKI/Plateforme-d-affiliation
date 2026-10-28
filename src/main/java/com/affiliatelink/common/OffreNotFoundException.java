package com.affiliatelink.common;

public class OffreNotFoundException extends RuntimeException {
    public OffreNotFoundException(Long id) {
        super("Offre introuvable : " + id);
    }
}
