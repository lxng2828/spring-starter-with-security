package com.vtlong.my_spring_boot_project.config;

import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vtlong.my_spring_boot_project.repository.InvalidatedTokenRepository;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtConfig jwtConfig;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(JwtConfig jwtConfig, InvalidatedTokenRepository invalidatedTokenRepository) {
        this.jwtConfig = jwtConfig;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String jti = claimsSet.getJWTID();

            if (invalidatedTokenRepository.existsById(jti)) {
                throw new JwtException("Token has been logged out");
            }
        } catch (Exception e) {
            throw new JwtException("Invalid token format: " + e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtConfig.getSignerKey().getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}