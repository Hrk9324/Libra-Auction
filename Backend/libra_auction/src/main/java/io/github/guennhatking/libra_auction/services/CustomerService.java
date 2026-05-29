package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.account.EmailStatus;
import io.github.guennhatking.libra_auction.enums.account.AccountStatus;
import io.github.guennhatking.libra_auction.models.account.Role;
import io.github.guennhatking.libra_auction.models.account.TaiKhoanOAuth;
import io.github.guennhatking.libra_auction.models.account.TaiKhoanPassword;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.account.RoleRepository;
import io.github.guennhatking.libra_auction.repositories.account.TaiKhoanOAuthRepository;
import io.github.guennhatking.libra_auction.repositories.account.TaiKhoanPasswordRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.ImageUploadedResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository nguoiDungRepository;
    private final TaiKhoanPasswordRepository taiKhoanPasswordRepository;
    private final TaiKhoanOAuthRepository taiKhoanOAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final ImageUploadService imageUploadService;    

    public CustomerService(CustomerRepository nguoiDungRepository,
            TaiKhoanPasswordRepository taiKhoanPasswordRepository,
            TaiKhoanOAuthRepository taiKhoanOAuthRepository,
            RoleRepository roleRepository,
            PasswordService passwordService,
            ImageUploadService imageUploadService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.taiKhoanPasswordRepository = taiKhoanPasswordRepository;
        this.taiKhoanOAuthRepository = taiKhoanOAuthRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.imageUploadService = imageUploadService;
    }

    @Transactional
    public Customer createPasswordUser(String email, String username, String password, String hoVaTen) {
        return createPasswordUser(email, username, password, hoVaTen, null, null, null);
    }

    @Transactional
    public Customer createPasswordUser(String email, String username, String password, String hoVaTen,
            String soDienThoai, String cccd, String anhDaiDien) {
        if (taiKhoanPasswordRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("The username already exists");
        }

        Customer user = new Customer(hoVaTen, email);
        user.setSoDienThoai(soDienThoai);
        user.setCccd(cccd);
        user.setAnhDaiDien(anhDaiDien);
        user.setTrangThaiEmail(EmailStatus.CHUA_XAC_THUC);
        user.setTrangThaiTaiKhoan(AccountStatus.CHO_XAC_NHAN);

        Customer savedUser = nguoiDungRepository.save(user);

        String encodedPassword = passwordService.encodePassword(password);
        TaiKhoanPassword taiKhoan = new TaiKhoanPassword(
                UUID.randomUUID().toString(),
                username,
                encodedPassword,
                new byte[0]);
        taiKhoan.setNguoiDung(savedUser);
        taiKhoan.setTrangThai(AccountStatus.CHO_XAC_NHAN);
        taiKhoanPasswordRepository.save(taiKhoan);

        savedUser = assignDefaultRole(savedUser);

        return savedUser;
    }

    public Customer createOAuthUser(String email, String googleId, String displayName, String pictureUrl) {
        Optional<Customer> existingUser = nguoiDungRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Customer user = new Customer(displayName, email);
        try {
            ImageUploadedResponse newUrl = imageUploadService.uploadImageFromUrl(pictureUrl, "avatars");
            user.setAnhDaiDien(newUrl.secureUrl());
        } catch (Exception e) {
            System.out.println("Failed to upload avatar for user " + email + ": " + e.getMessage());
        }
        user.setTrangThaiEmail(EmailStatus.DA_XAC_THUC);
        user.setTrangThaiTaiKhoan(AccountStatus.HOAT_DONG);

        Customer savedUser = nguoiDungRepository.save(user);

        TaiKhoanOAuth oauthAccount = new TaiKhoanOAuth(
                UUID.randomUUID().toString(),
                "google",
                googleId);
        oauthAccount.setNguoiDung(savedUser);
        oauthAccount.setTrangThai(AccountStatus.HOAT_DONG);
        taiKhoanOAuthRepository.save(oauthAccount);

        savedUser = assignDefaultRole(savedUser);

        return savedUser;
    }

    public Optional<Customer> findByEmail(String email) {
        return nguoiDungRepository.findByEmail(email);
    }

    public Optional<Customer> findById(String userId) {
        return nguoiDungRepository.findById(userId);
    }

    public Optional<TaiKhoanPassword> findPasswordAccountByUsername(String username) {
        return taiKhoanPasswordRepository.findByUsername(username);
    }

    public Optional<TaiKhoanOAuth> findOAuthAccountByProviderId(String providerId) {
        return taiKhoanOAuthRepository.findByProviderId(providerId);
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        TaiKhoanPassword account = taiKhoanPasswordRepository.findByNguoiDungId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại hoặc không dùng mật khẩu."));

        if (!passwordService.verifyPassword(currentPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }

        account.setPasswordHash(passwordService.encodePassword(newPassword));
        taiKhoanPasswordRepository.save(account);
    }

    @Transactional
    public void markEmailVerified(String email) {
        Customer customer = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email này."));

        customer.setTrangThaiEmail(EmailStatus.DA_XAC_THUC);
        if (customer.getTrangThaiTaiKhoan() == AccountStatus.CHO_XAC_NHAN) {
            customer.setTrangThaiTaiKhoan(AccountStatus.HOAT_DONG);
        }
        nguoiDungRepository.save(customer);

        taiKhoanPasswordRepository.findByNguoiDungEmail(email).ifPresent(acc -> {
            if (acc.getTrangThai() == AccountStatus.CHO_XAC_NHAN) {
                acc.setTrangThai(AccountStatus.HOAT_DONG);
                taiKhoanPasswordRepository.save(acc);
            }
        });
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        TaiKhoanPassword account = taiKhoanPasswordRepository.findByNguoiDungEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này."));

        account.setPasswordHash(passwordService.encodePassword(newPassword));
        taiKhoanPasswordRepository.save(account);
    }

        // Search pending users (accounts awaiting confirmation)
        public io.github.guennhatking.libra_auction.viewmodels.response.PageResponse<io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse>
            searchPendingUsers(Integer page, Integer pageSize) {

        java.util.List<Customer> all = nguoiDungRepository.findAll();

        java.util.List<Customer> filtered = all.stream()
            .filter(u -> u.getTrangThaiTaiKhoan() != null
                && u.getTrangThaiTaiKhoan() == AccountStatus.CHO_XAC_NHAN)
            .collect(java.util.stream.Collectors.toList());

        // Simple pagination logic
        int p = page != null ? page : 0;
        int ps = pageSize != null ? pageSize : 20;
        int totalElements = filtered.size();
        int totalPages = (totalElements + ps - 1) / ps;
        int start = Math.min(p * ps, totalElements);
        int end = Math.min(start + ps, totalElements);
        java.util.List<Customer> pageContent = filtered.subList(start, end);

        java.util.List<io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse> content = pageContent.stream()
            .map(user -> new io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse(
                user.getId(),
                user.getHoVaTen(),
                user.getSoDienThoai(),
                user.getCccd(),
                user.getEmail(),
                user.getAnhDaiDien(),
                user.getTrangThaiEmail(),
                user.getTrangThaiTaiKhoan()))
            .collect(java.util.stream.Collectors.toList());

        return new io.github.guennhatking.libra_auction.viewmodels.response.PageResponse<>(
            content,
            totalPages,
            (long) totalElements,
            p,
            ps,
            p == 0,
            p == Math.max(0, totalPages - 1)
        );
        }

    private Customer assignDefaultRole(Customer user) {
        Role defaultRole = roleRepository.findById("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "User")));

        user.setRoles(new ArrayList<>(List.of(defaultRole)));
        return nguoiDungRepository.save(user);
    }
}
