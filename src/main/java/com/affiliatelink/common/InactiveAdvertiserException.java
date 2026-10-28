package com.affiliatelink.common;

public class InactiveAdvertiserException extends RuntimeException {
    public InactiveAdvertiserException(Long id) {
        super("Annonceur inactif : " + id);
    }
}
