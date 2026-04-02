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
import io.github.guennhatking.libra_auction.security.JwtUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import lombok.extern.slf4j.Slf4j;
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
    private JwtUtils jwtUtils;

    @Value("${app.jwt.expiration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration:2592000}")
    private long REFRESHABLE_DURATION;

    /**
     * Xác thực Token sử dụng Public Key RSA
     */
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // Ép kiểu về RSAPublicKey để Nimbus chấp nhận
            RSAPublicKey publicKey = (RSAPublicKey) jwtUtils.getPublicKey();
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            Date expiryTime = isRefresh 
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                    .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

            var verified = signedJWT.verify(verifier);

            if (!(verified && expiryTime.after(new Date()))) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            String expectedType = isRefresh ? "REFRESH" : "ACCESS";
            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            
            if (tokenType == null || !tokenType.equals(expectedType)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return signedJWT;
        } catch (Exception e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Tạo Token sử dụng Private Key RSA (RS512)
     */
    private String generateToken(NguoiDung user, boolean isRefresh) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS512);

        long duration = isRefresh ? REFRESHABLE_DURATION : VALID_DURATION;
        String type = isRefresh ? "REFRESH" : "ACCESS";

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("io.github.guennhatking")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            // Ép kiểu về RSAPrivateKey để Nimbus ký
            RSAPrivateKey privateKey = (RSAPrivateKey) jwtUtils.getPrivateKey();
            jwsObject.sign(new RSASSASigner(privateKey));
            return jwsObject.serialize();
        } catch (Exception e) {
            log.error("RSA Signing failed", e);
            throw new RuntimeException("Token generation error", e);
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var taiKhoanPassword = taiKhoanPasswordRepository.findByUsername(request.getUsername());

        if (taiKhoanPassword == null || 
            !passwordEncoder.matches(request.getPassword(), taiKhoanPassword.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        var authUser = taiKhoanPassword.getNguoiDung();

        return AuthenticationResponse.builder()
                .token(generateToken(authUser, false))
                .refreshToken(generateToken(authUser, true))
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException, ParseException {
        var signedJWT = verifyToken(request.getRefresh_token(), true);
        String userId = signedJWT.getJWTClaimsSet().getSubject();
        
        var user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        
        return AuthenticationResponse.builder()
                .token(generateToken(user, false))
                .build();
    }

    private String buildScope(NguoiDung user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (user != null && user.getRoles() != null) {
            user.getRoles().forEach(role -> stringJoiner.add("ROLE_" + role.getName()));
        }
        return stringJoiner.toString();
    }

    public UserResponse signup(SignupRequest request) {
        System.out.println("=== [SERVICE] Bắt đầu Signup: " + request.getUsername());

        if (taiKhoanRepository.findByUsername(request.getUsername()) != null) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        String passwordHash = passwordEncoder.encode(request.getPassword());

        NguoiDung user = NguoiDung.builder()
                .hoVaTen(request.getFullName())
                .email(request.getEmail())
                .soDienThoai(request.getSoDienThoai())
                .CCCD(request.getCCCD())
                .trangThaiEmail(io.github.guennhatking.libra_auction.enums.Enums.TrangThaiEmail.CHO_XAC_THUC)
                .trangThaiTaiKhoan(io.github.guennhatking.libra_auction.enums.Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN)
                .thoiGianTao(java.time.LocalDateTime.now())
                .build();
        
        // Lưu để có ID, gán vào biến final để dùng trong Lambda
        final NguoiDung finalUser = nguoiDungRepository.save(user);

        // Gán Role USER (Fix lỗi UnsupportedOperationException bằng ArrayList)
        roleRepository.findById("USER").ifPresentOrElse(role -> {
            List<io.github.guennhatking.libra_auction.models.Role> mutableRoles = new ArrayList<>();
            mutableRoles.add(role);
            finalUser.setRoles(mutableRoles);
            nguoiDungRepository.save(finalUser);
            System.out.println(">>> Đã gán Role USER thành công.");
        }, () -> System.out.println("!!! CẢNH BÁO: Role USER chưa có trong DB."));

        // Tạo tài khoản mật khẩu
        TaiKhoanPassword taiKhoan = new TaiKhoanPassword(
            UUID.randomUUID().toString(), 
            request.getUsername(), 
            passwordHash, 
            null
        );
        taiKhoan.setNguoiDung(finalUser);
        taiKhoanPasswordRepository.save(taiKhoan);

        System.out.println("=== [SERVICE] Signup hoàn tất cho: " + finalUser.getId());

        return UserResponse.builder()
                .id(finalUser.getId())
                .hoVaTen(finalUser.getHoVaTen())
                .email(finalUser.getEmail())
                .roles(finalUser.getRoles() != null ? new HashSet<>(finalUser.getRoles()) : Collections.emptySet())
                .build();
    }

    private String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        Path uploadsDir = Paths.get("uploads/profile-images").toAbsolutePath();
        Files.createDirectories(uploadsDir);
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String uniqueFilename = userId + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = uploadsDir.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());
        return "uploads/profile-images/" + uniqueFilename;
    }
}