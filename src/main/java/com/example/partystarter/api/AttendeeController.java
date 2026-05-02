package com.example.partystarter.api;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.Attendee;
import com.example.partystarter.model.User;
import com.example.partystarter.model.request.AttendeeStatusUpdateBody;
import com.example.partystarter.model.response.AttendeeResponse;
import com.example.partystarter.model.response.PendingCountResponse;
import com.example.partystarter.service.AttendeeService;
import com.example.partystarter.service.JoinRequestService;
import com.example.partystarter.service.identity.CallerIdentity;
import com.example.partystarter.service.identity.CallerResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class AttendeeController {

    private final AttendeeService attendeeService;
    private final JoinRequestService joinRequestService;
    private final CallerResolver callerResolver;

    @GetMapping("/{id}/attendees")
    public ResponseEntity<List<AttendeeResponse>> list(@PathVariable Integer id, HttpServletRequest req) {
        Optional<CallerIdentity> caller = callerResolver.resolve(req);
        List<Attendee> rows = attendeeService.listAttendees(id, caller);
        return ResponseEntity.ok(rows.stream().map(JoinRequestController::toAttendeeResponse).toList());
    }

    @PutMapping("/{id}/attendees/me")
    public ResponseEntity<AttendeeResponse> updateMyStatus(
            @PathVariable Integer id,
            @RequestBody AttendeeStatusUpdateBody body,
            HttpServletRequest req) {
        if (body == null || body.status() == null) {
            throw new ResourceException(HttpStatus.BAD_REQUEST, "status is required");
        }
        CallerIdentity caller = callerResolver.resolve(req)
            .orElseThrow(() -> new ResourceException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        Attendee updated = attendeeService.setStatus(id, caller, body.status());
        return ResponseEntity.ok(JoinRequestController.toAttendeeResponse(updated));
    }

    @GetMapping("/requests/count")
    public ResponseEntity<PendingCountResponse> pendingCount(HttpServletRequest req) {
        User caller = callerResolver.requireAuthenticatedUser(req);
        return ResponseEntity.ok(new PendingCountResponse(joinRequestService.pendingCountForCreator(caller)));
    }
}
