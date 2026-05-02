package com.example.partystarter.model;

import com.example.partystarter.model.enums.JoinRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "join_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id")
    private User requesterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_guest_id")
    private GuestUser requesterGuest;

    // columnDefinition pins VARCHAR so Hibernate 6's default Java-enum → MySQL-ENUM
    // mapping doesn't trip schema-validate against the migration's VARCHAR(16) column.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(16)")
    private JoinRequestStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
