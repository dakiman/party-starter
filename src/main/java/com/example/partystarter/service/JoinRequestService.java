package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.enums.AttendeeStatus;
import com.example.partystarter.model.enums.JoinRequestStatus;
import com.example.partystarter.repo.AttendeeRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.JoinRequestRepository;
import com.example.partystarter.service.identity.AuthenticatedUser;
import com.example.partystarter.service.identity.CallerIdentity;
import com.example.partystarter.service.identity.Guest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JoinRequestService {

    private final EventRepository eventRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final AttendeeRepository attendeeRepository;

    public enum SubmitOutcome { PENDING, ATTENDING, ALREADY_PENDING, ALREADY_ATTENDING, ALREADY_DECLINED }

    public record SubmitResult(SubmitOutcome outcome, JoinRequest request, Attendee attendee) {}

    @Transactional
    public SubmitResult submit(String shareToken, CallerIdentity caller) {
        Event event = eventRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Share link is invalid or has been revoked"));

        // Already attending? short-circuit
        Optional<Attendee> existingAttendee = findAttendee(event, caller);
        if (existingAttendee.isPresent()) {
            return new SubmitResult(SubmitOutcome.ALREADY_ATTENDING, null, existingAttendee.get());
        }

        if (Boolean.TRUE.equals(event.getIsPrivate())) {
            return submitPrivateEventRequest(event, caller);
        }
        return submitPublicEventAttendance(event, caller);
    }

    private SubmitResult submitPrivateEventRequest(Event event, CallerIdentity caller) {
        Optional<JoinRequest> existing = findRequest(event, caller);
        if (existing.isPresent()) {
            JoinRequest jr = existing.get();
            return switch (jr.getStatus()) {
                case PENDING -> new SubmitResult(SubmitOutcome.ALREADY_PENDING, jr, null);
                case DECLINED -> new SubmitResult(SubmitOutcome.ALREADY_DECLINED, jr, null);
                case APPROVED -> {
                    // Approved but somehow no attendee row — recreate and surface attending.
                    Attendee a = ensureAttendee(event, caller);
                    yield new SubmitResult(SubmitOutcome.ALREADY_ATTENDING, jr, a);
                }
            };
        }

        JoinRequest fresh = JoinRequest.builder()
            .event(event)
            .requesterUser(caller instanceof AuthenticatedUser au ? au.user() : null)
            .requesterGuest(caller instanceof Guest g ? g.guestUser() : null)
            .status(JoinRequestStatus.PENDING)
            .build();
        return new SubmitResult(SubmitOutcome.PENDING, joinRequestRepository.save(fresh), null);
    }

    private SubmitResult submitPublicEventAttendance(Event event, CallerIdentity caller) {
        Attendee a = ensureAttendee(event, caller);
        return new SubmitResult(SubmitOutcome.ATTENDING, null, a);
    }

    @Transactional(readOnly = true)
    public List<JoinRequest> listPending(Integer eventId, User caller) {
        Event event = loadEventForCreator(eventId, caller);
        return joinRequestRepository.findByEventIdAndStatusOrderByCreatedAtAsc(
            event.getId(), JoinRequestStatus.PENDING);
    }

    @Transactional
    public Attendee approve(Integer eventId, Long requestId, User caller) {
        Event event = loadEventForCreator(eventId, caller);
        JoinRequest jr = loadRequestForEvent(requestId, event);

        if (jr.getStatus() == JoinRequestStatus.APPROVED) {
            // Idempotent — return existing attendee
            return ensureAttendeeForRequest(event, jr);
        }
        if (jr.getStatus() == JoinRequestStatus.DECLINED) {
            throw new ResourceException(HttpStatus.CONFLICT,
                "This request was declined and cannot be approved.");
        }

        jr.setStatus(JoinRequestStatus.APPROVED);
        jr.setDecidedAt(LocalDateTime.now());
        joinRequestRepository.save(jr);
        return ensureAttendeeForRequest(event, jr);
    }

    @Transactional
    public void decline(Integer eventId, Long requestId, User caller) {
        Event event = loadEventForCreator(eventId, caller);
        JoinRequest jr = loadRequestForEvent(requestId, event);

        if (jr.getStatus() == JoinRequestStatus.DECLINED) {
            return; // idempotent
        }
        if (jr.getStatus() == JoinRequestStatus.APPROVED) {
            throw new ResourceException(HttpStatus.CONFLICT,
                "This request was already approved.");
        }
        jr.setStatus(JoinRequestStatus.DECLINED);
        jr.setDecidedAt(LocalDateTime.now());
        joinRequestRepository.save(jr);
    }

    @Transactional(readOnly = true)
    public long pendingCountForCreator(User creator) {
        return joinRequestRepository.countPendingForCreator(creator.getId());
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private Event loadEventForCreator(Integer eventId, User caller) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Event not found"));
        if (event.getCreator() == null || !event.getCreator().getId().equals(caller.getId())) {
            throw new ResourceException(HttpStatus.FORBIDDEN, "You are not the creator of this event");
        }
        return event;
    }

    private JoinRequest loadRequestForEvent(Long requestId, Event event) {
        JoinRequest jr = joinRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Join request not found"));
        if (!jr.getEvent().getId().equals(event.getId())) {
            // Don't differentiate "no such request" from "request belongs to another event" — same response prevents probing.
            throw new ResourceException(HttpStatus.NOT_FOUND, "Join request not found");
        }
        return jr;
    }

    private Optional<JoinRequest> findRequest(Event event, CallerIdentity caller) {
        if (caller instanceof AuthenticatedUser au) {
            return joinRequestRepository.findByEventIdAndRequesterUser(event.getId(), au.user());
        }
        return joinRequestRepository.findByEventIdAndRequesterGuest(event.getId(), ((Guest) caller).guestUser());
    }

    private Optional<Attendee> findAttendee(Event event, CallerIdentity caller) {
        if (caller instanceof AuthenticatedUser au) {
            return attendeeRepository.findByEventIdAndUser(event.getId(), au.user());
        }
        return attendeeRepository.findByEventIdAndGuest(event.getId(), ((Guest) caller).guestUser());
    }

    private Attendee ensureAttendee(Event event, CallerIdentity caller) {
        return findAttendee(event, caller).orElseGet(() -> attendeeRepository.save(Attendee.builder()
            .event(event)
            .user(caller instanceof AuthenticatedUser au ? au.user() : null)
            .guest(caller instanceof Guest g ? g.guestUser() : null)
            .status(AttendeeStatus.GOING)
            .build()));
    }

    private Attendee ensureAttendeeForRequest(Event event, JoinRequest jr) {
        if (jr.getRequesterUser() != null) {
            return attendeeRepository.findByEventIdAndUser(event.getId(), jr.getRequesterUser())
                .orElseGet(() -> attendeeRepository.save(Attendee.builder()
                    .event(event).user(jr.getRequesterUser())
                    .status(AttendeeStatus.GOING).build()));
        }
        return attendeeRepository.findByEventIdAndGuest(event.getId(), jr.getRequesterGuest())
            .orElseGet(() -> attendeeRepository.save(Attendee.builder()
                .event(event).guest(jr.getRequesterGuest())
                .status(AttendeeStatus.GOING).build()));
    }
}
