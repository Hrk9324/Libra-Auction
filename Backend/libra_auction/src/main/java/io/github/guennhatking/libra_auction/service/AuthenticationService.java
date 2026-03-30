package io.github.guennhatking.libra_auction.service;


import io.github.guennhatking.libra_auction.dto.request.RefreshRequest;
import io.github.guennhatking.libra_auction.dto.request.SignupRequest;
import io.github.guennhatking.libra_auction.dto.response.AuthenticationResponse;
import io.github.guennhatking.libra_auction.dto.response.UserResponse;
import io.github.guennhatking.libra_auction.exception.AppException;
import io.github.guennhatking.libra_auction.exception.ErrorCode;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.TaiKhoanPassword;
import io.github.guennhatking.libra_auction.repos.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repos.TaiKhoanRepository;
import io.github.guennhatking.libra_auction.repos.TaiKhoanPasswordRepository;
import io.github.guennhatking.libra_auction.repos.RoleRepository;
import io.github.guennhatking.libra_auction.dto.request.AuthenticationRequest;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import io.github.guennhatking.libra_auction.util.RSAKeyProvider;

import java.util.UUID;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AuthenticationService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    
    @Autowired
    private TaiKhoanPasswordRepository taiKhoanPasswordRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RSAKeyProvider rsaKeyProvider;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION;

    
    private SignedJWT verifyAccessToken(String token) throws JOSEException, ParseException, java.text.ParseException {
        JWSVerifier verifier = new RSASSAVerifier(rsaKeyProvider.getPublicKey());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
        if (tokenType == null || !tokenType.equals("ACCESS")) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException, java.text.ParseException {
        JWSVerifier verifier = new RSASSAVerifier(rsaKeyProvider.getPublicKey());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli());

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
        if (tokenType == null || !tokenType.equals("REFRESH")) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }



    private String buildScope(NguoiDung user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        try {
            if (user != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
                user.getRoles().forEach(role -> {
                    stringJoiner.add("ROLE_" + role);
                });
            }
        } catch (Exception e) {
            log.warn("Failed to build scope for user", e);
        }

        return stringJoiner.toString();
    }

    private String generateToken(NguoiDung user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("io.github.guennhatking")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "ACCESS")
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new RSASSASigner(rsaKeyProvider.getPrivateKey()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(NguoiDung user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("io.github.guennhatking")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "REFRESH")
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new RSASSASigner(rsaKeyProvider.getPrivateKey()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        
        if (request.getUsername() == null || request.getPassword() == null)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var taiKhoanPassword = taiKhoanPasswordRepository
                .findByUsername(request.getUsername());

        if (taiKhoanPassword == null)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String passwordHash = taiKhoanPassword.getPasswordHash();

        if (passwordHash == null)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        boolean authenticated =
                passwordEncoder.matches(request.getPassword(), passwordHash);

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        
        var authUser = taiKhoanPassword.getNguoiDung();

        var token = generateToken(authUser);
        var refreshToken = generateRefreshToken(authUser);

        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) 
            throws JOSEException, ParseException, java.text.ParseException {
        var signedJWT = verifyRefreshToken(request.getRefresh_token());
        String userId = signedJWT.getJWTClaimsSet().getSubject();
        
        var user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        
        var token = generateToken(user);
        
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public UserResponse signup(SignupRequest request) {
        // Check if username already exists
        var existingTaiKhoan = taiKhoanRepository.findByUsername(request.getUsername());
        if (existingTaiKhoan != null) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Validate inputs
        if (request.getUsername().length() < 3) {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }
        if (request.getUsername().contains(" ")) {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }
        if (request.getPassword().length() < 6) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // Validate password format (must contain uppercase, lowercase, digit, special char)
        try {
            TaiKhoanPassword.validatePasswordFormat(request.getPassword());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // Create user with password hash
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Create NguoiDung using builder
        NguoiDung user = NguoiDung.builder()
                .hoVaTen(request.getFullName())
                .email(request.getEmail())
                .soDienThoai(request.getSoDienThoai())
                .CCCD(request.getCCCD())
                .trangThaiEmail(io.github.guennhatking.libra_auction.enums.Enums.TrangThaiEmail.CHO_XAC_THUC)
                .trangThaiTaiKhoan(io.github.guennhatking.libra_auction.enums.Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN)
                .thoiGianTao(java.time.LocalDateTime.now())
                .build();
        
        user = nguoiDungRepository.save(user);

        // Assign default USER role
        var userRole = roleRepository.findById("USER");
        if (userRole.isPresent()) {
            java.util.List<io.github.guennhatking.libra_auction.models.Role> roles = new java.util.ArrayList<>();
            roles.add(userRole.get());
            user.setRoles(roles);
            user = nguoiDungRepository.save(user);
            log.info("Assigned USER role to user: {}", user.getId());
        } else {
            log.warn("USER role not found in database. Skipping role assignment.");
        }

        // Handle profile image upload
        if (request.getAnhDaiDien() != null && !request.getAnhDaiDien().isEmpty()) {
            try {
                String imagePath = uploadProfileImage(user.getId(), request.getAnhDaiDien());
                user.setAnhDaiDien(imagePath);
                user = nguoiDungRepository.save(user);
                log.info("Uploaded profile image for user: {}", user.getId());
            } catch (IOException e) {
                log.warn("Failed to upload profile image for user: {}", user.getId(), e);
                // Continue without image, don't fail the signup
            }
        }

        // Create TaiKhoanPassword using constructor
        TaiKhoanPassword taiKhoan = new TaiKhoanPassword(
                UUID.randomUUID().toString(),
                request.getUsername(),
                passwordHash,
                null   // salt
        );
        taiKhoan.setNguoiDung(user);
        
        taiKhoanPasswordRepository.save(taiKhoan);

        // Return user response with all fields
        return UserResponse.builder()
                .id(user.getId())
                .hoVaTen(user.getHoVaTen())
                .soDienThoai(user.getSoDienThoai())
                .CCCD(user.getCCCD())
                .email(user.getEmail())
                .anhDaiDien(user.getAnhDaiDien())
                .trangThaiEmail(user.getTrangThaiEmail())
                .trangThaiTaiKhoan(user.getTrangThaiTaiKhoan())
                .roles(user.getRoles() != null ? user.getRoles().stream().collect(java.util.stream.Collectors.toSet()) : java.util.Collections.emptySet())
                .build();
    }

    private String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        // Create uploads directory if it doesn't exist
        Path uploadsDir = Paths.get("uploads/profile-images").toAbsolutePath();
        Files.createDirectories(uploadsDir);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFilename = userId + "_" + System.currentTimeMillis() + fileExtension;

        // Save file
        Path filePath = uploadsDir.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        // Return relative path for storing in database
        return "uploads/profile-images/" + uniqueFilename;
    }
}