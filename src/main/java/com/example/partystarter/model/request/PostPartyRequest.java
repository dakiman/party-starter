package com.example.partystarter.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class PostPartyRequest {
    @Size(min = 1, message = "You must choose at least one drink")
    private List<Integer> drinks;
    @Length(min = 3, max = 40)
    private String name;
}
