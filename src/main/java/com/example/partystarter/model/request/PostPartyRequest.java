package com.example.partystarter.model.request;

import com.example.partystarter.model.Drink;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
public class PostPartyRequest {
    private List<Drink> drinks;
    @Nullable
    private String name;
}
