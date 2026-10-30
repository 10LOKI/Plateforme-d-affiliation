package com.affiliatelink.offer.internal.repository;

import com.affiliatelink.offer.internal.entity.Offre;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Locale;

public class OffreSpecifications {
    private OffreSpecifications() {
    }

    public static Specification<Offre> withFilters(String niche, Long advertiserId,
                                                  String tag, String keyword) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (niche != null && !niche.isBlank()) {
                predicates.add(builder.equal(root.get("niche"), niche.trim()));
            }
            if (advertiserId != null) {
                predicates.add(builder.equal(root.get("advertiserId"), advertiserId));
            }
            if (tag != null && !tag.isBlank()) {
                query.distinct(true);
                predicates.add(builder.equal(root.join("tags").get("name"),
                        tag.trim().toLowerCase(Locale.ROOT)));
            }
            if (keyword != null && !keyword.isBlank()) {
                String escaped = keyword.trim().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(builder.like(builder.lower(root.get("title")), "%" + escaped + "%", '\\'));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
