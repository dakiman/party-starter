-- Seed initial values for Hibernate-managed sequence tables.
--
-- The TABLE strategy generators (drink, drink_ingredient, event, ingredient, user)
-- read next_val from these tables. Hibernate does not seed them on its own when
-- Flyway owns the schema, so we seed here. Without this, the very first INSERT
-- for any of these entities fails with:
--   "could not read a hi value - you need to populate the table: <name>_seq"
INSERT INTO drink_seq (next_val) VALUES (1);
INSERT INTO drink_ingredient_seq (next_val) VALUES (1);
INSERT INTO event_seq (next_val) VALUES (1);
INSERT INTO ingredient_seq (next_val) VALUES (1);
INSERT INTO user_seq (next_val) VALUES (1);
