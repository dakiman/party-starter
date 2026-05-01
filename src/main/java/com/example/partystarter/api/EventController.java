package com.example.partystarter.api;

import com.example.partystarter.model.enums.EventFilter;
import com.example.partystarter.model.request.PostEventRequest;
import com.example.partystarter.model.request.PutEventRequest;
import com.example.partystarter.model.response.EventResponse;
import com.example.partystarter.model.response.ShareLinkResponse;
import com.example.partystarter.service.EventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/events")
public class EventController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createNewEvent(@Valid @RequestBody PostEventRequest request) {
        return ResponseEntity.ok(eventService.saveEvent(request));
    }

    @GetMapping("/public")
    public ResponseEntity<Page<EventResponse>> getPublicEvents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "includePast", defaultValue = "false") boolean includePast) {
        int clampedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, clampedSize);
        return ResponseEntity.ok(eventService.getPublicEvents(q, includePast, pageable));
    }

    @GetMapping("/share/{token}")
    public ResponseEntity<EventResponse> getEventByShareToken(@PathVariable("token") String token) {
        return ResponseEntity.ok(eventService.getEventByShareToken(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable(value = "id") Integer id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(
            @RequestParam(name = "createdBy") EventFilter filter) {
        return ResponseEntity.ok(eventService.getEvents(filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable(value = "id") Integer id,
            @Valid @RequestBody PutEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable(value = "id") Integer id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ShareLinkResponse> issueShareLink(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(eventService.issueShareToken(id));
    }

    @PostMapping("/{id}/share/rotate")
    public ResponseEntity<ShareLinkResponse> rotateShareLink(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(eventService.rotateShareToken(id));
    }
}
