CREATE TABLE guest_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    display_name  VARCHAR(64)  NOT NULL,
    discriminator CHAR(4)      NOT NULL,
    contact_note  VARCHAR(255) NULL,
    guest_token   CHAR(36)     NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_guest_user_name_disc (display_name, discriminator),
    UNIQUE KEY uk_guest_user_token (guest_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE join_request (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    event_id              INT          NOT NULL,
    requester_user_id     BIGINT       NULL,
    requester_guest_id    BIGINT       NULL,
    status                VARCHAR(16)  NOT NULL,
    created_at            DATETIME(6)  NOT NULL,
    decided_at            DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_jr_event FOREIGN KEY (event_id)           REFERENCES event(id),
    CONSTRAINT fk_jr_user  FOREIGN KEY (requester_user_id)  REFERENCES user(id),
    CONSTRAINT fk_jr_guest FOREIGN KEY (requester_guest_id) REFERENCES guest_user(id),
    CONSTRAINT chk_jr_one_requester CHECK (
        (requester_user_id IS NULL) <> (requester_guest_id IS NULL)
    ),
    UNIQUE KEY uk_jr_event_user  (event_id, requester_user_id),
    UNIQUE KEY uk_jr_event_guest (event_id, requester_guest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE attendee (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    event_id      INT         NOT NULL,
    user_id       BIGINT      NULL,
    guest_id      BIGINT      NULL,
    status        VARCHAR(16) NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_att_event FOREIGN KEY (event_id) REFERENCES event(id),
    CONSTRAINT fk_att_user  FOREIGN KEY (user_id)  REFERENCES user(id),
    CONSTRAINT fk_att_guest FOREIGN KEY (guest_id) REFERENCES guest_user(id),
    CONSTRAINT chk_att_one_attendee CHECK (
        (user_id IS NULL) <> (guest_id IS NULL)
    ),
    UNIQUE KEY uk_att_event_user  (event_id, user_id),
    UNIQUE KEY uk_att_event_guest (event_id, guest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
