package com.example.partystarter.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@RequiredArgsConstructor
public class LoginResponse {
    @NonNull
    private String token;
}
