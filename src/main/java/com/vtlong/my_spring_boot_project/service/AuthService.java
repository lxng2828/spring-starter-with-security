package com.vtlong.my_spring_boot_project.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
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
import com.vtlong.my_spring_boot_project.dto.request.LogoutRequest;
import com.vtlong.my_spring_boot_project.dto.request.RefreshTokenRequest;
import com.vtlong.my_spring_boot_project.dto.response.IntrospectResponse;
import com.vtlong.my_spring_boot_project.dto.response.LoginResponse;
import com.vtlong.my_spring_boot_project.dto.response.RefreshTokenResponse;
import com.vtlong.my_spring_boot_project.exception.AppException;
import com.vtlong.my_spring_boot_project.exception.ErrorCode;
import com.vtlong.my_spring_boot_project.model.InvalidatedToken;
import com.vtlong.my_spring_boot_project.model.User;
import com.vtlong.my_spring_boot_project.repository.UserRepository;
import com.vtlong.my_spring_boot_project.repository.InvalidatedTokenRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtConfig jwtConfig,
            InvalidatedTokenRepository invalidatedTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
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

    public void handleLogout(LogoutRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean isValid = verifyToken(token);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        String jti = claimsSet.getJWTID();
        Date expirationTime = claimsSet.getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder().id(jti).expiresAt(expirationTime).build());
    }

    public RefreshTokenResponse handleRefreshToken(RefreshTokenRequest refreshTokenRequest)
            throws JOSEException, ParseException {
        String token = refreshTokenRequest.getToken();

        boolean isValid = verifyToken(token);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Invalid refresh token");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        String userEmail = claimsSet.getSubject();
        String userId = (String) claimsSet.getClaim("userId");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String jti = claimsSet.getJWTID();
        Date expirationTime = claimsSet.getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(jti)
                .expiresAt(expirationTime)
                .build());

        String newToken = generateToken(user);

        return RefreshTokenResponse.builder()
                .success(true)
                .token(newToken)
                .message("Token làm mới thành công")
                .build();
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
                .jwtID(UUID.randomUUID().toString())
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

        boolean isValid = verifyToken(token);

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private boolean verifyToken(String token) throws JOSEException, ParseException {
        try {
            JWSVerifier verifier = new MACVerifier(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8));
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (expirationTime.before(new Date())) {
                return false;
            }

            boolean isValid = signedJWT.verify(verifier);
            if (!isValid) {
                return false;
            }

            if (!signedJWT.getJWTClaimsSet().getAudience().contains("my-spring-app")) {
                return false;
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String jti = claimsSet.getJWTID();

            if (invalidatedTokenRepository.existsById(jti)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
