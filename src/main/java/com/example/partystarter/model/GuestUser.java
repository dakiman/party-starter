package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "guest_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

    @Column(nullable = false, columnDefinition = "char(4)")
    private String discriminator;

    @Column(name = "contact_note", length = 255)
    private String contactNote;

    @JsonIgnore
    @Column(name = "guest_token", nullable = false, unique = true, columnDefinition = "char(36)")
    private String guestToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
