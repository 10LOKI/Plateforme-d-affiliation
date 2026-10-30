package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.entity.Offre;
import com.affiliatelink.offer.internal.repository.CampaignRepository;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Transactional
class CampaignCreationIT {
    @Autowired WebApplicationContext context;
    @Autowired OffreRepository offres;
    @Autowired CampaignRepository campaigns;
    @Autowired EntityManager entityManager;
    private MockMvc mvc;
    private long advertiserId;
    private Long offreId;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        advertiserId = advertiser(true);
        var offre = new Offre();
        offre.setAdvertiserId(advertiserId);
        offre.setTitle("Offre campagnes");
        offre.setDescription("Description");
        offre.setLandingUrl("https://example.com/produit");
        offre.setNiche("TECH");
        offreId = offres.saveAndFlush(offre).getId();
        entityManager.clear();
    }

    @Test
    void createsDraftCampaignsForEveryModelAndPercentageBoundaries() throws Exception {
        long before = campaigns.count();
        for (String[] payout : List.of(new String[]{"CPC", "0.5"}, new String[]{"CPL", "2"},
                new String[]{"CPA_FIXED", "10"}, new String[]{"CPA_PERCENT", "0"},
                new String[]{"CPA_PERCENT", "100"})) {
            String response = mvc.perform(post("/api/offres/" + offreId + "/campaigns")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(advertiserId, payout[0], payout[1])))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.payoutModel").value(payout[0]))
                    .andExpect(jsonPath("$.offre").doesNotExist())
                    .andReturn().getResponse().getContentAsString();
            Number id = JsonPath.read(response, "$.id");
            entityManager.clear();
            assertEquals(offreId, campaigns.findById(id.longValue()).orElseThrow().getOffre().getId());
        }
        assertEquals(before + 5, campaigns.count());
        mvc.perform(get("/api/offres/" + offreId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.campaigns.length()").value(5));
    }

    @Test
    void rejectsInvalidDatesBudgetsAndPayoutsWithoutSaving() throws Exception {
        long before = campaigns.count();
        String valid = body(advertiserId, "CPC", "0.5");
        for (String invalid : List.of(
                valid.replace("2026-11-30", "2026-11-01"),
                valid.replace("2026-11-30", "2026-10-31"),
                valid.replace("500.00", "0"),
                valid.replace("500.00", "-1"),
                valid.replace("500.00", "0.00001"),
                body(advertiserId, "CPC", "0"),
                body(advertiserId, "CPL", "0"),
                body(advertiserId, "CPA_FIXED", "0"),
                body(advertiserId, "CPA_PERCENT", "101"),
                body(advertiserId, "CPA_PERCENT", "-1"),
                body(advertiserId, "UNKNOWN", "1"), "{}")) {
            mvc.perform(post("/api/offres/" + offreId + "/campaigns")
                    .contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(before, campaigns.count());
    }

    @Test
    void returns404ForAMissingOfferOrAdvertiser() throws Exception {
        long before = campaigns.count();
        mvc.perform(post("/api/offres/" + Long.MAX_VALUE + "/campaigns")
                .contentType(MediaType.APPLICATION_JSON).content(body(advertiserId, "CPC", "1")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/offres/" + offreId + "/campaigns")
                .contentType(MediaType.APPLICATION_JSON).content(body(Long.MAX_VALUE, "CPC", "1")))
                .andExpect(status().isNotFound());
        assertEquals(before, campaigns.count());
    }

    @Test
    void returns409ForAnInactiveAdvertiserOrAnotherOwnersOffer() throws Exception {
        long before = campaigns.count();
        long anotherId = advertiser(true);
        mvc.perform(post("/api/offres/" + offreId + "/campaigns")
                .contentType(MediaType.APPLICATION_JSON).content(body(anotherId, "CPC", "1")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
        mvc.perform(patch("/api/admin/profiles/" + advertiserId + "/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/offres/" + offreId + "/campaigns")
                .contentType(MediaType.APPLICATION_JSON).content(body(advertiserId, "CPC", "1")))
                .andExpect(status().isConflict());
        assertEquals(before, campaigns.count());
    }

    private long advertiser(boolean active) throws Exception {
        String response = mvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s@example.com","password":"TestJunior123!",
                         "companyName":"Demo","sector":"TECH","website":"https://example.com"}
                        """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        if (active) {
            mvc.perform(patch("/api/admin/profiles/" + id.longValue() + "/status")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                    .andExpect(status().isNoContent());
        }
        return id.longValue();
    }

    private String body(long advertiserId, String model, String value) {
        return """
                {"advertiserId":%d,"name":"Campagne novembre","startDate":"2026-11-01",
                 "endDate":"2026-11-30","budget":500.00,"payoutModel":"%s","payoutValue":%s}
                """.formatted(advertiserId, model, value);
    }
}
