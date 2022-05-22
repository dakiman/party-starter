package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    String name;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "party_drinks",
            joinColumns = @JoinColumn(name = "party_id"),
            inverseJoinColumns = @JoinColumn(name = "drink_id"))
    private List<Drink> drinks = new LinkedList<>();

}
