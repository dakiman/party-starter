package com.example.partystarter.repo;

import com.example.partystarter.model.GuestUser;
import com.example.partystarter.model.JoinRequest;
import com.example.partystarter.model.User;
import com.example.partystarter.model.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    Optional<JoinRequest> findByEventIdAndRequesterUser(Integer eventId, User user);

    Optional<JoinRequest> findByEventIdAndRequesterGuest(Integer eventId, GuestUser guest);

    List<JoinRequest> findByEventIdAndStatusOrderByCreatedAtAsc(Integer eventId, JoinRequestStatus status);

    @Query("""
        select count(jr) from JoinRequest jr
        where jr.event.creator.id = :creatorId
          and jr.status = com.example.partystarter.model.enums.JoinRequestStatus.PENDING
    """)
    long countPendingForCreator(@Param("creatorId") Long creatorId);
}
