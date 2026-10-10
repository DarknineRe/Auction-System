-- Constraints protect new writes without blocking deploys on legacy rows.
-- Validate each constraint after auditing and correcting any existing violations.
ALTER TABLE biddings
    ADD CONSTRAINT ck_biddings_prices
        CHECK (starting_price IS NOT NULL AND starting_price > 0
            AND minimum_bid_increment IS NOT NULL AND minimum_bid_increment > 0) NOT VALID,
    ADD CONSTRAINT ck_biddings_dates
        CHECK (start_date IS NOT NULL AND end_date IS NOT NULL AND end_date > start_date) NOT VALID,
    ADD CONSTRAINT ck_biddings_status
        CHECK (status IN ('ACTIVE', 'CLOSED', 'CANCELLED')) NOT VALID,
    ADD CONSTRAINT ck_biddings_rating
        CHECK (seller_rating IS NULL OR seller_rating BETWEEN 1 AND 5) NOT VALID;

ALTER TABLE bidactions
    ADD CONSTRAINT ck_bidactions_amount CHECK (amount > 0) NOT VALID,
    ADD CONSTRAINT ck_bidactions_status CHECK (status IN ('VALID', 'VOIDED')) NOT VALID;

ALTER TABLE payments
    ADD CONSTRAINT ck_payments_amount CHECK (amount > 0) NOT VALID,
    ADD CONSTRAINT ck_payments_status
        CHECK (status IN ('AWAITING_PAYMENT', 'PAYMENT_SUBMITTED', 'PAID', 'COMPLETED', 'EXPIRED', 'CANCELLED'))
        NOT VALID;

ALTER TABLE seller_profiles
    ADD CONSTRAINT ck_seller_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5) NOT VALID;

CREATE INDEX IF NOT EXISTS idx_bidding_artworks_artwork ON bidding_artworks (artwork_id);
CREATE INDEX IF NOT EXISTS idx_bidactions_top
    ON bidactions (bidding_id, status, amount DESC);
