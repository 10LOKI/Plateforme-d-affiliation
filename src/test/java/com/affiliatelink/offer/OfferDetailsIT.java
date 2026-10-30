package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.entity.*;
import com.affiliatelink.offer.internal.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Transactional
class OfferDetailsIT {
    @Autowired WebApplicationContext context;
    @Autowired OffreRepository offres;
    @Autowired CampaignRepository campaigns;
    @Autowired EntityManager entityManager;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void returnsAnOfferWithEmptyCollections() throws Exception {
        var offre = newOffre();
        Long id = offre.getId();
        entityManager.clear();
        mvc.perform(get("/api/offres/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Offre détail"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.tags").isArray()).andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.campaigns").isArray()).andExpect(jsonPath("$.campaigns").isEmpty());
    }

    @Test
    void returnsOnlyThisOffersCampaignsWithoutAnEntityBackReference() throws Exception {
        var offre = newOffre();
        var other = newOffre();
        addCampaign(offre, "Campagne A");
        addCampaign(offre, "Campagne B");
        addCampaign(other, "Autre offre");
        Long id = offre.getId();
        entityManager.clear();
        mvc.perform(get("/api/offres/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.campaigns.length()").value(2))
                .andExpect(jsonPath("$.campaigns[*].name", containsInAnyOrder("Campagne A", "Campagne B")))
                .andExpect(jsonPath("$.campaigns[0].id").isNumber())
                .andExpect(jsonPath("$.campaigns[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.campaigns[0].payoutModel").value("CPC"))
                .andExpect(jsonPath("$.campaigns[0].payoutValue").value(0.5))
                .andExpect(jsonPath("$.campaigns[0].startDate").value("2026-11-01"))
                .andExpect(jsonPath("$.campaigns[0].offre").doesNotExist());
    }

    @Test
    void returns404ForAMissingOffer() throws Exception {
        mvc.perform(get("/api/offres/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
    }

    private Offre newOffre() {
        var offre = new Offre();
        offre.setAdvertiserId(123L);
        offre.setTitle("Offre détail");
        offre.setDescription("Description");
        offre.setLandingUrl("https://example.com/detail");
        offre.setNiche("TECH");
        return offres.saveAndFlush(offre);
    }

    private void addCampaign(Offre offre, String name) {
        var campaign = new Campaign();
        campaign.setName(name);
        campaign.setStartDate(LocalDate.of(2026, 11, 1));
        campaign.setEndDate(LocalDate.of(2026, 11, 30));
        campaign.setBudget(new BigDecimal("500.0000"));
        campaign.setPayoutModel(PayoutModel.CPC);
        campaign.setPayoutValue(new BigDecimal("0.5000"));
        offre.addCampaign(campaign);
        campaigns.saveAndFlush(campaign);
    }
}
