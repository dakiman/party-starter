package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.Attendee;
import com.example.partystarter.model.Event;
import com.example.partystarter.model.User;
import com.example.partystarter.model.enums.AttendeeStatus;
import com.example.partystarter.repo.AttendeeRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.service.identity.AuthenticatedUser;
import com.example.partystarter.service.identity.CallerIdentity;
import com.example.partystarter.service.identity.Guest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AttendeeService {

    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;

    /**
     * Visibility:
     *   1. event public → anyone (caller may be empty)
     *   2. caller is creator → visible
     *   3. caller has an attendee row → visible
     *   4. otherwise 403
     */
    @Transactional(readOnly = true)
    public List<Attendee> listAttendees(Integer eventId, Optional<CallerIdentity> caller) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Event not found"));

        if (Boolean.FALSE.equals(event.getIsPrivate())) {
            return attendeeRepository.findByEventIdOrderByCreatedAtAsc(event.getId());
        }
        if (caller.isEmpty()) {
            throw new ResourceException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        if (isCreator(event, caller.get()) || hasAttendeeRow(event, caller.get())) {
            return attendeeRepository.findByEventIdOrderByCreatedAtAsc(event.getId());
        }
        throw new ResourceException(HttpStatus.FORBIDDEN, "You don't have access to this attendee list");
    }

    @Transactional
    public Attendee setStatus(Integer eventId, CallerIdentity caller, AttendeeStatus status) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Event not found"));
        Attendee a = findAttendee(event, caller)
            .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "You are not on the attendee list"));
        a.setStatus(status);
        return attendeeRepository.save(a);
    }

    private boolean isCreator(Event event, CallerIdentity caller) {
        if (!(caller instanceof AuthenticatedUser au)) return false;
        return event.getCreator() != null && event.getCreator().getId().equals(au.user().getId());
    }

    private boolean hasAttendeeRow(Event event, CallerIdentity caller) {
        if (caller instanceof AuthenticatedUser au) {
            return attendeeRepository.existsByEventIdAndUser(event.getId(), au.user());
        }
        return attendeeRepository.existsByEventIdAndGuest(event.getId(), ((Guest) caller).guestUser());
    }

    private Optional<Attendee> findAttendee(Event event, CallerIdentity caller) {
        if (caller instanceof AuthenticatedUser au) {
            return attendeeRepository.findByEventIdAndUser(event.getId(), au.user());
        }
        return attendeeRepository.findByEventIdAndGuest(event.getId(), ((Guest) caller).guestUser());
    }
}
