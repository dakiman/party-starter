package com.example.partystarter.model.mapper;

import com.example.partystarter.model.Artist;
import com.example.partystarter.model.Event;
import com.example.partystarter.model.Location;
import com.example.partystarter.model.response.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class EventMapper {


    @Mapping(target = "food", source = "foodItems")
    public abstract EventResponse eventToEventResponse(Event event);

    public abstract EventResponse.ArtistResponse artistToArtistResponse(Artist artist);

    public abstract EventResponse.LocationResponse locationToLocationResponse(Location location);
}
