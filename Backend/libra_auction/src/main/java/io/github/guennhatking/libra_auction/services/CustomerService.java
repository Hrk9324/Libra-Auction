package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.account.EmailStatus;
import io.github.guennhatking.libra_auction.enums.account.AccountStatus;
import io.github.guennhatking.libra_auction.models.account.Role;
import io.github.guennhatking.libra_auction.models.account.AccountOAuth;
import io.github.guennhatking.libra_auction.models.account.AccountPassword;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.account.RoleRepository;
import io.github.guennhatking.libra_auction.repositories.account.AccountOAuthRepository;
import io.github.guennhatking.libra_auction.repositories.account.AccountPasswordRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.CustomerProfileUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.ImageUploadedResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountPasswordRepository accountPasswordRepository;
    private final AccountOAuthRepository accountOAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final ImageUploadService imageUploadService;

    public CustomerService(CustomerRepository customerRepository,
            AccountPasswordRepository accountPasswordRepository,
            AccountOAuthRepository accountOAuthRepository,
            RoleRepository roleRepository,
            PasswordService passwordService,
            ImageUploadService imageUploadService) {
        this.customerRepository = customerRepository;
        this.accountPasswordRepository = accountPasswordRepository;
        this.accountOAuthRepository = accountOAuthRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.imageUploadService = imageUploadService;
    }

    @Transactional
    public Customer createPasswordUser(String email, String username, String password, String fullName) {
        return createPasswordUser(email, username, password, fullName, null, null, null);
    }

    @Transactional
    public Customer createPasswordUser(String email, String username, String password, String fullName,
            String phoneNumber, String identityNumber, String avatarUrl) {
        if (accountPasswordRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("The username already exists");
        }

        Customer user = new Customer(fullName, email);
        user.setPhoneNumber(phoneNumber);
        user.setIdentityNumber(identityNumber);
        user.setAvatarUrl(avatarUrl);
        user.setEmailStatus(EmailStatus.UNVERIFIED);
        user.setAccountStatus(AccountStatus.PENDING);

        Customer savedUser = customerRepository.save(user);

        String encodedPassword = passwordService.encodePassword(password);
        AccountPassword account = new AccountPassword(
                UUID.randomUUID().toString(),
                username,
                encodedPassword,
                new byte[0]);
        account.setCustomer(savedUser);
        account.setStatus(AccountStatus.PENDING);
        accountPasswordRepository.save(account);

        savedUser = assignDefaultRole(savedUser);

        return savedUser;
    }

    public Customer createOAuthUser(String email, String googleId, String displayName, String pictureUrl) {
        Optional<Customer> existingUser = customerRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Customer user = new Customer(displayName, email);
        try {
            ImageUploadedResponse newUrl = imageUploadService.uploadImageFromUrl(pictureUrl, "avatars");
            user.setAvatarUrl(newUrl.secureUrl());
        } catch (Exception e) {
            System.out.println("Failed to upload avatar for user " + email + ": " + e.getMessage());
        }
        user.setEmailStatus(EmailStatus.VERIFIED);
        user.setAccountStatus(AccountStatus.ACTIVE);

        Customer savedUser = customerRepository.save(user);

        AccountOAuth oauthAccount = new AccountOAuth(
                UUID.randomUUID().toString(),
                "google",
                googleId);
        oauthAccount.setCustomer(savedUser);
        oauthAccount.setStatus(AccountStatus.ACTIVE);
        accountOAuthRepository.save(oauthAccount);

        savedUser = assignDefaultRole(savedUser);

        return savedUser;
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findById(String userId) {
        return customerRepository.findById(userId);
    }

    @Transactional
    public Customer updateProfile(String userId, CustomerProfileUpdateRequest request) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        customer.setFullName(request.fullName());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setIdentityNumber(request.identityNumber());
        customer.setAvatarUrl(request.avatarUrl());

        return customerRepository.save(customer);
    }

    public Optional<AccountPassword> findPasswordAccountByUsername(String username) {
        return accountPasswordRepository.findByUsername(username);
    }

    public Optional<AccountOAuth> findOAuthAccountByProviderId(String providerId) {
        return accountOAuthRepository.findByProviderId(providerId);
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        AccountPassword account = accountPasswordRepository.findByCustomerId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai hoac khong dung mat khau."));

        if (!passwordService.verifyPassword(currentPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Mat khau hien tai khong dung.");
        }

        account.setPasswordHash(passwordService.encodePassword(newPassword));
        accountPasswordRepository.save(account);
    }

    @Transactional
    public void markEmailVerified(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung voi email nay."));

        customer.setEmailStatus(EmailStatus.VERIFIED);
        if (customer.getAccountStatus() == AccountStatus.PENDING) {
            customer.setAccountStatus(AccountStatus.ACTIVE);
        }
        customerRepository.save(customer);

        accountPasswordRepository.findByCustomerEmail(email).ifPresent(acc -> {
            if (acc.getStatus() == AccountStatus.PENDING) {
                acc.setStatus(AccountStatus.ACTIVE);
                accountPasswordRepository.save(acc);
            }
        });
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        AccountPassword account = accountPasswordRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan voi email nay."));

        account.setPasswordHash(passwordService.encodePassword(newPassword));
        accountPasswordRepository.save(account);
    }

    public io.github.guennhatking.libra_auction.viewmodels.response.PageResponse<io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse>
            searchPendingUsers(Integer page, Integer pageSize) {
        return searchAdminUsers(page, pageSize, null, null, null, null, null, AccountStatus.PENDING);
    }

    public io.github.guennhatking.libra_auction.viewmodels.response.PageResponse<io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse>
            searchAdminUsers(
                    Integer page,
                    Integer pageSize,
                    String name,
                    String email,
                    String phone,
                    String identityNumber,
                    EmailStatus emailStatus,
                    AccountStatus accountStatus) {

        java.util.List<Customer> filtered = customerRepository.findAll().stream()
            .filter(user -> containsIgnoreCase(user.getFullName(), name))
            .filter(user -> containsIgnoreCase(user.getEmail(), email))
            .filter(user -> containsIgnoreCase(user.getPhoneNumber(), phone))
            .filter(user -> containsIgnoreCase(user.getIdentityNumber(), identityNumber))
            .filter(user -> emailStatus == null || user.getEmailStatus() == emailStatus)
            .filter(user -> accountStatus == null || user.getAccountStatus() == accountStatus)
            .collect(java.util.stream.Collectors.toList());

        return buildAdminUserPage(filtered, page, pageSize);
    }

    @Transactional
    public io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse updateAdminUserStatus(
            String userId,
            AccountStatus status) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        customer.setAccountStatus(status);
        customerRepository.save(customer);

        accountPasswordRepository.findByCustomerId(userId).ifPresent(account -> {
            account.setStatus(status);
            accountPasswordRepository.save(account);
        });

        return toAdminUserResponse(customer);
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        return value != null && value.toLowerCase().contains(query.trim().toLowerCase());
    }

    private io.github.guennhatking.libra_auction.viewmodels.response.PageResponse<io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse> buildAdminUserPage(
            java.util.List<Customer> filtered,
            Integer page,
            Integer pageSize) {
        int p = page != null ? page : 0;
        int ps = pageSize != null ? pageSize : 20;
        int totalElements = filtered.size();
        int totalPages = (totalElements + ps - 1) / ps;
        int start = Math.min(p * ps, totalElements);
        int end = Math.min(start + ps, totalElements);

        java.util.List<io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse> content = filtered.subList(start, end).stream()
            .map(this::toAdminUserResponse)
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

    private io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse toAdminUserResponse(Customer user) {
        return new io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse(
            user.getId(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getIdentityNumber(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getEmailStatus(),
            user.getAccountStatus());
    }
    private Customer assignDefaultRole(Customer user) {
        Role defaultRole = roleRepository.findById("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "User")));

        user.setRole(defaultRole);
        return customerRepository.save(user);
    }
}
