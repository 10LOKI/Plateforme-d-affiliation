package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.entity.*;
import com.affiliatelink.offer.internal.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Transactional
class OfferPersistenceIT {
    @Autowired OffreRepository offres;
    @Autowired CampaignRepository campaigns;
    @Autowired TagRepository tags;
    @Autowired EntityManager entityManager;

    @Test
    void reloadsBidirectionalRelationsAndSharesOneNormalizedTag() {
        var tag = new Tag();
        String name = "tech-" + UUID.randomUUID();
        tag.setName("  " + name.toUpperCase(java.util.Locale.ROOT) + "  ");
        tags.saveAndFlush(tag);
        var first = newOffre("Première offre");
        var second = newOffre("Deuxième offre");
        first.addTag(tag);
        second.addTag(tag);
        offres.save(first);
        offres.save(second);
        var campaign = new Campaign();
        campaign.setName("Campagne de test");
        campaign.setStartDate(LocalDate.of(2026, 11, 1));
        campaign.setEndDate(LocalDate.of(2026, 11, 30));
        campaign.setBudget(new BigDecimal("500.0000"));
        campaign.setPayoutModel(PayoutModel.CPC);
        campaign.setPayoutValue(new BigDecimal("0.5000"));
        first.addCampaign(campaign);
        campaigns.saveAndFlush(campaign);
        Long firstId = first.getId();
        Long tagId = tag.getId();
        Long campaignId = campaign.getId();
        entityManager.clear();

        var savedOffre = offres.findById(firstId).orElseThrow();
        assertNotNull(savedOffre.getCreatedAt());
        assertEquals(999999L, savedOffre.getAdvertiserId());
        assertEquals(1, savedOffre.getCampaigns().size());
        assertEquals(campaignId, savedOffre.getCampaigns().get(0).getId());
        assertEquals(tagId, savedOffre.getTags().iterator().next().getId());
        var savedTag = tags.findByName(name).orElseThrow();
        assertEquals(name, savedTag.getName());
        assertEquals(2, savedTag.getOffres().size());
        var savedCampaign = campaigns.findById(campaignId).orElseThrow();
        assertEquals(firstId, savedCampaign.getOffre().getId());
        assertEquals(CampaignStatus.DRAFT, savedCampaign.getStatus());
        assertEquals(new BigDecimal("0.5000"), savedCampaign.getPayoutValue());
        assertEquals(LocalDate.of(2026, 11, 30), savedCampaign.getEndDate());
        assertEquals("DRAFT", entityManager.createNativeQuery(
                "select status from offer_campaigns where id = :id")
                .setParameter("id", campaignId).getSingleResult());
    }

    @Test
    void rejectsDuplicateTagNamesInTheDatabase() {
        String name = "duplicate-" + UUID.randomUUID();
        var first = new Tag();
        first.setName(name);
        tags.saveAndFlush(first);
        var duplicate = new Tag();
        duplicate.setName(name.toUpperCase(java.util.Locale.ROOT));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> tags.saveAndFlush(duplicate));
    }

    private Offre newOffre(String title) {
        var offre = new Offre();
        offre.setAdvertiserId(999999L);
        offre.setTitle(title);
        offre.setDescription("Description de test");
        offre.setLandingUrl("https://example.com/produit");
        offre.setNiche("TECH");
        return offre;
    }
}
