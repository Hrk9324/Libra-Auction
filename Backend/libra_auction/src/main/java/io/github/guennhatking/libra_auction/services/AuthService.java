package io.github.guennhatking.libra_auction.services;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import io.github.guennhatking.libra_auction.dto.response.JwtResponse;
import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.TaiKhoanOAuth;
import io.github.guennhatking.libra_auction.repos.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repos.TaiKhoanOAuthRepository;
import io.github.guennhatking.libra_auction.security.JwtUtils;

@Service
public class AuthService {

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Value("${app.google.client-secret}")
    private String googleClientSecret;

    @Autowired
    private TaiKhoanOAuthRepository taiKhoanOAuthRepo;

    @Autowired
    private NguoiDungRepository nguoiDungRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public JwtResponse authenticateWithGoogle(String authCode) throws Exception {
        
        String idTokenString;
        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    "https://oauth2.googleapis.com/token",
                    googleClientId,
                    googleClientSecret,
                    authCode,
                    "postmessage"
            ).execute();

            idTokenString = tokenResponse.getIdToken();
            System.out.println("Da doi Code thanh ID Token thanh cong!");

        } catch (Exception e) {
            System.out.println("Loi khi doi Code: " + e.getMessage());
            throw new IllegalArgumentException("Ma code khong hop le hoac da het han!");
        }

        // BƯỚC 2: XÁC MINH ID TOKEN (Giữ nguyên logic cũ)
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();

            // BƯỚC 3: KIỂM TRA DATABASE VÀ LƯU NGƯỜI DÙNG
            Optional<TaiKhoanOAuth> existingAccount = taiKhoanOAuthRepo.findByProviderAndProviderId("GOOGLE", googleId);
            NguoiDung user;

            if (existingAccount.isPresent()) {
                user = existingAccount.get().getNguoiDung();
            } else {
                NguoiDung newUser = new NguoiDung();
                newUser.setHoVaTen(name);
                newUser.setEmail(email);
                newUser.setTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN);
                user = nguoiDungRepo.save(newUser);

                TaiKhoanOAuth newOAuthAccount = new TaiKhoanOAuth(UUID.randomUUID().toString(), email, "GOOGLE", googleId);
                newOAuthAccount.setNguoiDung(user);
                taiKhoanOAuthRepo.save(newOAuthAccount);
                System.out.println("Đã tạo người dùng mới: " + email);
            }

            // BƯỚC 4: TẠO JWT CỦA HỆ THỐNG TRẢ VỀ
            String jwt = jwtUtils.generateJwtToken(user.getId(), email);
            return new JwtResponse(jwt, user.getId(), user.getHoVaTen(), email);

        } else {
            throw new IllegalArgumentException("Xác minh ID Token thất bại!");
        }
    }
}