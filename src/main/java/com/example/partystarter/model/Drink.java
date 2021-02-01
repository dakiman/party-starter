package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Drink {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // hide from json resposne
    private Integer id;

    private String name;

    @Column(length = 10000)
    private String recipe;

    private int externalId;

    private boolean alcoholic;

}
