CREATE TABLE offer_offres (
    id BIGSERIAL PRIMARY KEY,
    advertiser_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    landing_url TEXT NOT NULL,
    niche VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE offer_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT offer_tag_normalized CHECK (name = lower(btrim(name)) AND name <> '')
);

CREATE TABLE offer_offre_tags (
    offre_id BIGINT NOT NULL REFERENCES offer_offres(id),
    tag_id BIGINT NOT NULL REFERENCES offer_tags(id),
    PRIMARY KEY (offre_id, tag_id)
);

CREATE TABLE offer_campaigns (
    id BIGSERIAL PRIMARY KEY,
    offre_id BIGINT NOT NULL REFERENCES offer_offres(id),
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget NUMERIC(19,4) NOT NULL,
    payout_model VARCHAR(255) NOT NULL,
    payout_value NUMERIC(19,4) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT offer_campaign_dates CHECK (end_date > start_date),
    CONSTRAINT offer_campaign_budget CHECK (budget > 0),
    CONSTRAINT offer_campaign_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ENDED')),
    CONSTRAINT offer_campaign_payout CHECK (
        (payout_model IN ('CPC', 'CPL', 'CPA_FIXED') AND payout_value > 0)
        OR (payout_model = 'CPA_PERCENT' AND payout_value BETWEEN 0 AND 100)
    )
);
