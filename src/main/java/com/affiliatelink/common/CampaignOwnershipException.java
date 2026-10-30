package com.affiliatelink.common;

public class CampaignOwnershipException extends RuntimeException {
    public CampaignOwnershipException() {
        super("Cette offre n’appartient pas à cet annonceur.");
    }
}
