package com.example.partystarter.config;

import com.example.partystarter.service.SpotifyAuthService;
import feign.RequestInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;

@AllArgsConstructor
public class FeignConfig {

    private final SpotifyAuthService spotifyAuthService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + spotifyAuthService.getSpotifyToken());
            requestTemplate.header("Content-Length", "0");
        };
    }

}
