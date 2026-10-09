ALTER TABLE biddings
    ADD COLUMN minimum_bid_increment NUMERIC(19, 4) NOT NULL DEFAULT 1.00;
