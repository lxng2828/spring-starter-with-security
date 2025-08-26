package com.vtlong.my_spring_boot_project.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vtlong.my_spring_boot_project.config.JwtConfig;
import com.vtlong.my_spring_boot_project.dto.request.IntrospectRequest;
import com.vtlong.my_spring_boot_project.dto.request.LoginRequest;
import com.vtlong.my_spring_boot_project.dto.response.IntrospectResponse;
import com.vtlong.my_spring_boot_project.dto.response.LoginResponse;
import com.vtlong.my_spring_boot_project.exception.AppException;
import com.vtlong.my_spring_boot_project.exception.ErrorCode;
import com.vtlong.my_spring_boot_project.model.User;
import com.vtlong.my_spring_boot_project.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    public LoginResponse handleLogin(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        return LoginResponse.builder().success(true).token(token).build();
    }

    private List<String> buildScopes(Set<String> roleNames) {
        return roleNames.stream()
                .map(role -> role)
                .collect(Collectors.toList());
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().getCode())
                .collect(Collectors.toSet());

        JWTClaimsSet jwsClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("vtlong.com")
                .audience("my-spring-app")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * jwtConfig.getExpirationHours()))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("scope", buildScopes(roleNames))
                .build();

        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwsClaimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return jwsObject.serialize();
    }

    public IntrospectResponse handleIntrospect(IntrospectRequest introspectRequest)
            throws JOSEException, ParseException {
        String token = introspectRequest.getToken();
        JWSVerifier verifier = new MACVerifier(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8));
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (expirationTime.before(new Date())) {
            return IntrospectResponse.builder().valid(false).build();
        }

        boolean isValid = signedJWT.verify(verifier);
        if (!isValid) {
            return IntrospectResponse.builder().valid(false).build();
        }

        if (!signedJWT.getJWTClaimsSet().getAudience().contains("my-spring-app")) {
            return IntrospectResponse.builder().valid(false).build();
        }

        return IntrospectResponse.builder().valid(true).build();
    }
}
