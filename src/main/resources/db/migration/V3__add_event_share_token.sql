ALTER TABLE event
  ADD COLUMN share_token CHAR(36) NULL,
  ADD CONSTRAINT uk_event_share_token UNIQUE (share_token);

CREATE INDEX idx_event_public_date ON event (is_private, date);
