package com.example.partystarter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drink {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // hide from json resposne
    private Integer id;

    private String name;
    private String recipe;
}
