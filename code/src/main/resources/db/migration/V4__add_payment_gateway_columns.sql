-- PromptPay QR payments: the provider's charge and the QR code currently shown to the buyer.
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS gateway_charge_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS qr_image_url      VARCHAR(2048),
    ADD COLUMN IF NOT EXISTS qr_expires_at     TIMESTAMP(6);
