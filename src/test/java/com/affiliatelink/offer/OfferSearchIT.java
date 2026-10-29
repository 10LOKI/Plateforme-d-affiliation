package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.entity.Offre;
import com.affiliatelink.offer.internal.entity.Tag;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.offer.internal.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Transactional
class OfferSearchIT {
    @Autowired WebApplicationContext context;
    @Autowired OffreRepository offres;
    @Autowired TagRepository tags;
    private MockMvc mvc;
    private String token;
    private Offre alpha;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        token = UUID.randomUUID().toString();
        var shared = tag(token);
        alpha = offer("Alpha " + token, token, 99001L);
        var beta = offer("Beta " + token, token, 99002L);
        var gamma = offer("Gamma " + token, "other-" + token, 99001L);
        alpha.addTag(shared);
        alpha.addTag(tag("extra-" + token));
        beta.addTag(shared);
        offres.saveAndFlush(alpha);
        offres.saveAndFlush(beta);
        offres.saveAndFlush(gamma);
    }

    @Test
    void combinesOptionalFiltersAndDoesNotDuplicateResults() throws Exception {
        mvc.perform(get("/api/offres").param("tag", " " + token.toUpperCase(java.util.Locale.ROOT) + " "))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
        mvc.perform(get("/api/offres").param("niche", token).param("advertiserId", "99001")
                .param("tag", token).param("keyword", "ALPHA " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(alpha.getId()));
        mvc.perform(get("/api/offres").param("niche", token).param("advertiserId", "99001")
                .param("keyword", "Gamma " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/offres"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(offres.count()));
    }

    @Test
    void paginatesAndSortsInBothDirections() throws Exception {
        mvc.perform(get("/api/offres").param("keyword", token).param("size", "1").param("sort", "title,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Gamma " + token))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(3));
        mvc.perform(get("/api/offres").param("keyword", token).param("size", "1")
                .param("page", "1").param("sort", "title,asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Beta " + token))
                .andExpect(jsonPath("$.page").value(1));
        mvc.perform(get("/api/offres").param("keyword", token).param("size", "1").param("page", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void treatsSqlWildcardCharactersAsLiteralText() throws Exception {
        offres.saveAndFlush(offer("100% Java " + token, token, 99001L));
        offres.saveAndFlush(offer("100X Java " + token, token, 99001L));
        mvc.perform(get("/api/offres").param("keyword", "100% Java " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void returns400ForInvalidPaginationAndSorting() throws Exception {
        for (var invalid : Map.of("page", "-1", "size", "0", "sort", "tags.name,asc", "advertiserId", "0").entrySet()) {
            mvc.perform(get("/api/offres").param(invalid.getKey(), invalid.getValue()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        }
        mvc.perform(get("/api/offres").param("page", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/offres").param("sort", "title,up")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/offres").param("size", "101")).andExpect(status().isBadRequest());
    }

    private Offre offer(String title, String niche, Long advertiserId) {
        var offre = new Offre();
        offre.setAdvertiserId(advertiserId);
        offre.setTitle(title);
        offre.setDescription("Recherche de test");
        offre.setLandingUrl("https://example.com/test");
        offre.setNiche(niche);
        return offre;
    }

    private Tag tag(String name) {
        var tag = new Tag();
        tag.setName(name);
        return tags.saveAndFlush(tag);
    }
}
