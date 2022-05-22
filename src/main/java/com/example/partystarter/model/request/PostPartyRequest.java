package com.example.partystarter.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PostPartyRequest {
    @NotNull
    private List<Integer> drinks;
    @Length(min = 3, max = 40)
    private String name;
}
