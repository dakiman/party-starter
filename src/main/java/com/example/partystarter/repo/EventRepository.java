package com.example.partystarter.repo;

import com.example.partystarter.model.Event;
import com.example.partystarter.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByCreatorOrderByCreatedAtDesc(User creator);

    Optional<Event> findByShareToken(String shareToken);

    /**
     * Discover-feed query.
     *
     * - Filters to public events only.
     * - Optional name LIKE filter (case-insensitive). Empty/null query returns all.
     * - Optional `since` lower bound on date — when non-null, hides past events.
     * - Sorts by date ASC with NULLS LAST (the case-when trick is JPQL-portable
     *   across dialects that don't accept the literal "NULLS LAST" syntax).
     */
    @Query("""
        select e from Event e
        where e.isPrivate = false
          and (:q is null or :q = '' or lower(e.name) like lower(concat('%', :q, '%')))
          and (:since is null or e.date >= :since)
        order by case when e.date is null then 1 else 0 end, e.date asc
    """)
    Page<Event> findPublic(@Param("q") String q,
                           @Param("since") LocalDate since,
                           Pageable pageable);
}
