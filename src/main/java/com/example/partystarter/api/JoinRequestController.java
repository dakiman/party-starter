package com.example.partystarter.api;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.enums.JoinRequestStatus;
import com.example.partystarter.model.request.JoinRequestSubmitBody;
import com.example.partystarter.model.response.*;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.JoinRequestRepository;
import com.example.partystarter.repo.AttendeeRepository;
import com.example.partystarter.service.GuestUserService;
import com.example.partystarter.service.JoinRequestService;
import com.example.partystarter.service.JoinRequestService.SubmitOutcome;
import com.example.partystarter.service.JoinRequestService.SubmitResult;
import com.example.partystarter.service.identity.AuthenticatedUser;
import com.example.partystarter.service.identity.CallerIdentity;
import com.example.partystarter.service.identity.CallerResolver;
import com.example.partystarter.service.identity.Guest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class JoinRequestController {

    private final EventRepository eventRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final AttendeeRepository attendeeRepository;
    private final JoinRequestService joinRequestService;
    private final GuestUserService guestUserService;
    private final CallerResolver callerResolver;

    // ── POST /share/{shareToken}/request ─────────────────────────────────────

    /**
     * Submit a join request (private events) or claim attendance (public events).
     *
     * <p><b>Known limitation — anonymous double-submit race.</b> Two simultaneous
     * POSTs from the same anonymous browser each allocate a fresh GuestUser. The
     * unique constraint on guest_user is (display_name, discriminator), not on a
     * browser session, so duplicate guests can result. FE-side debouncing (the
     * RequestDialog disables submit while in-flight) is the practical mitigation.
     */
    @PostMapping("/share/{shareToken}/request")
    public ResponseEntity<JoinRequestResponse> submitRequest(
            @PathVariable String shareToken,
            @RequestBody(required = false) JoinRequestSubmitBody body,
            HttpServletRequest httpRequest) {

        Optional<CallerIdentity> resolved = callerResolver.resolve(httpRequest);

        // Validate the share_token early (before any guest_user creation) so we don't
        // leak orphan rows if the token is unknown.
        if (resolved.isPresent()) {
            // Already-resolved identity (auth'd user OR returning guest) — service handles 404.
            SubmitResult result = joinRequestService.submit(shareToken, resolved.get());
            return ResponseEntity.ok(new JoinRequestResponse(null, stateLabel(result.outcome())));
        }

        // Anonymous: must have a display_name in the body. Validate the share_token
        // by attempting the lookup before allocating a GuestUser.
        if (body == null || body.displayName() == null || body.displayName().isBlank()) {
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Display name is required for guest requests");
        }
        eventRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Share link is invalid or has been revoked"));

        GuestUser g = guestUserService.createNew(body.displayName(), body.contactNote());
        CallerIdentity caller = new Guest(g);
        SubmitResult result = joinRequestService.submit(shareToken, caller);
        return ResponseEntity.ok(new JoinRequestResponse(g.getGuestToken(), stateLabel(result.outcome())));
    }

    // ── GET /share/{shareToken}/me ───────────────────────────────────────────

    @GetMapping("/share/{shareToken}/me")
    public ResponseEntity<ShareViewerStateResponse> me(
            @PathVariable String shareToken,
            HttpServletRequest httpRequest) {
        Event event = eventRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Share link is invalid or has been revoked"));
        boolean isPrivate = Boolean.TRUE.equals(event.getIsPrivate());

        Optional<CallerIdentity> caller = callerResolver.resolve(httpRequest);
        if (caller.isEmpty()) {
            return ResponseEntity.ok(new ShareViewerStateResponse("not_requested", null, null, false, isPrivate));
        }

        CallerIdentity ci = caller.get();
        boolean isCreator = ci instanceof AuthenticatedUser au && event.getCreator() != null
            && event.getCreator().getId().equals(au.user().getId());

        // 1. Attendee row?
        Optional<Attendee> a = (ci instanceof AuthenticatedUser au)
            ? attendeeRepository.findByEventIdAndUser(event.getId(), au.user())
            : attendeeRepository.findByEventIdAndGuest(event.getId(), ((Guest) ci).guestUser());
        if (a.isPresent()) {
            String state = isPrivate ? "approved" : "attending";
            return ResponseEntity.ok(new ShareViewerStateResponse(
                state, a.get().getStatus().name(), null, isCreator, isPrivate));
        }

        // 2. Pending/declined request?
        if (isPrivate) {
            Optional<JoinRequest> jr = (ci instanceof AuthenticatedUser au)
                ? joinRequestRepository.findByEventIdAndRequesterUser(event.getId(), au.user())
                : joinRequestRepository.findByEventIdAndRequesterGuest(event.getId(), ((Guest) ci).guestUser());
            if (jr.isPresent()) {
                JoinRequestStatus s = jr.get().getStatus();
                String label = switch (s) {
                    case PENDING -> "pending";
                    case APPROVED -> "approved";  // race: approved but no attendee yet
                    case DECLINED -> "declined";
                };
                return ResponseEntity.ok(new ShareViewerStateResponse(
                    label, null, jr.get().getDecidedAt(), isCreator, isPrivate));
            }
        }

        return ResponseEntity.ok(new ShareViewerStateResponse("not_requested", null, null, isCreator, isPrivate));
    }

    // ── GET /events/{id}/requests ────────────────────────────────────────────

    @GetMapping("/events/{id}/requests")
    public ResponseEntity<List<PendingRequestResponse>> listRequests(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        User caller = callerResolver.requireAuthenticatedUser(httpRequest);
        List<JoinRequest> pending = joinRequestService.listPending(id, caller);
        return ResponseEntity.ok(pending.stream().map(JoinRequestController::toPendingResponse).toList());
    }

    // ── POST /events/{id}/requests/{requestId}/approve|decline ──────────────

    @PostMapping("/events/{id}/requests/{requestId}/approve")
    public ResponseEntity<AttendeeResponse> approve(
            @PathVariable Integer id,
            @PathVariable Long requestId,
            HttpServletRequest httpRequest) {
        User caller = callerResolver.requireAuthenticatedUser(httpRequest);
        Attendee a = joinRequestService.approve(id, requestId, caller);
        return ResponseEntity.ok(toAttendeeResponse(a));
    }

    @PostMapping("/events/{id}/requests/{requestId}/decline")
    public ResponseEntity<Void> decline(
            @PathVariable Integer id,
            @PathVariable Long requestId,
            HttpServletRequest httpRequest) {
        User caller = callerResolver.requireAuthenticatedUser(httpRequest);
        joinRequestService.decline(id, requestId, caller);
        return ResponseEntity.noContent().build();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static String stateLabel(SubmitOutcome o) {
        return switch (o) {
            case PENDING -> "pending";
            case ATTENDING -> "attending";
            case ALREADY_PENDING -> "already_pending";
            case ALREADY_ATTENDING -> "already_attending";
            case ALREADY_DECLINED -> "already_declined";
        };
    }

    private static PendingRequestResponse toPendingResponse(JoinRequest jr) {
        PendingRequestResponse.RequesterIdentity ri;
        if (jr.getRequesterUser() != null) {
            User u = jr.getRequesterUser();
            ri = new PendingRequestResponse.RequesterIdentity("USER", u.getUsername(), null, null, u.getUsername());
        } else {
            GuestUser g = jr.getRequesterGuest();
            ri = new PendingRequestResponse.RequesterIdentity("GUEST", g.getDisplayName(), g.getDiscriminator(), g.getContactNote(), null);
        }
        return new PendingRequestResponse(jr.getId(), ri, jr.getCreatedAt());
    }

    static AttendeeResponse toAttendeeResponse(Attendee a) {
        AttendeeResponse.AttendeeIdentity ai;
        if (a.getUser() != null) {
            User u = a.getUser();
            ai = new AttendeeResponse.AttendeeIdentity("USER", u.getUsername(), null, u.getUsername());
        } else {
            GuestUser g = a.getGuest();
            ai = new AttendeeResponse.AttendeeIdentity("GUEST", g.getDisplayName(), g.getDiscriminator(), null);
        }
        return new AttendeeResponse(a.getId(), ai, a.getStatus().name());
    }
}
