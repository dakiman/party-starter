package com.example.partystarter.api;

import com.example.partystarter.model.request.PostEventRequest;
import com.example.partystarter.model.response.EventResponse;
import com.example.partystarter.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/events")
public class EventController {

    private final EventService eventService;

    @PostMapping(path = "")
    public ResponseEntity<EventResponse> createNewEvent(@Valid @RequestBody PostEventRequest request) {
        return ResponseEntity.ok(eventService.saveEvent(request));
    }
    
    @GetMapping(path = "/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable(value = "id") Integer id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }
} 