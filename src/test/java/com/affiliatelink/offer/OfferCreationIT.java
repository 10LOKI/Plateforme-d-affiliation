package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.offer.internal.repository.TagRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Transactional
class OfferCreationIT {
    @Autowired WebApplicationContext context;
    @Autowired OffreRepository offres;
    @Autowired TagRepository tags;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createsAnOfferAndReusesNormalizedTags() throws Exception {
        long advertiserId = createAdvertiser();
        mvc.perform(patch("/api/admin/profiles/" + advertiserId + "/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNoContent());
        String uniqueTag = "tech-" + UUID.randomUUID();
        String body = offerBody(advertiserId, "https://example.com/produit",
                "[\" " + uniqueTag.toUpperCase(java.util.Locale.ROOT) + " \",\"" + uniqueTag + "\",\"sport\"]");
        String response = mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.advertiserId").value(advertiserId))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[*].name", hasItems(uniqueTag, "sport")))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        var saved = offres.findById(id.longValue()).orElseThrow();
        assertEquals("Formation", saved.getTitle());
        assertEquals(2, saved.getTags().size());
        long tagCount = tags.count();
        Long tagId = tags.findByName(uniqueTag).orElseThrow().getId();
        mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        assertEquals(tagCount, tags.count());
        assertEquals(tagId, tags.findByName(uniqueTag).orElseThrow().getId());
    }

    @Test
    void refusesMissingAndInactiveAdvertisersWithoutSavingAnything() throws Exception {
        long countBefore = offres.count();
        long tagsBefore = tags.count();
        mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON)
                .content(offerBody(Long.MAX_VALUE, "https://example.com", "[\"tech\"]")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
        long advertiserId = createAdvertiser();
        mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON)
                .content(offerBody(advertiserId, "https://example.com", "[\"tech\"]")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
        assertEquals(countBefore, offres.count());
        assertEquals(tagsBefore, tags.count());
    }

    @Test
    void rejectsInvalidInputBeforeTheBusinessChecks() throws Exception {
        mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").exists());
        mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON)
                .content(offerBody(1L, "ftp://example.com", "[]")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.landingUrl").exists());
        mvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON)
                .content(offerBody(1L, "https://example.com", "[\"   \"]")))
                .andExpect(status().isBadRequest());
    }

    private long createAdvertiser() throws Exception {
        String body = """
                {"email":"%s@example.com","password":"TestJunior123!",
                 "companyName":"Demo","sector":"TECH","website":"https://example.com"}
                """.formatted(UUID.randomUUID());
        String response = mvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private String offerBody(long advertiserId, String url, String tagJson) {
        return """
                {"advertiserId":%d,"title":"Formation","description":"Une formation",
                 "landingUrl":"%s","niche":"TECH","tags":%s}
                """.formatted(advertiserId, url, tagJson);
    }
}
