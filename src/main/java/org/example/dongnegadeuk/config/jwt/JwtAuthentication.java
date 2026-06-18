package org.example.dongnegadeuk.config.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtAuthentication {
    private Long userId;
    private String username;
}
