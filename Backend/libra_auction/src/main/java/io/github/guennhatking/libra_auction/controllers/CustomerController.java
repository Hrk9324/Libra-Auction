package io.github.guennhatking.libra_auction.controllers;

import java.util.Optional;

import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.guennhatking.libra_auction.mappers.CustomerMapper;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.viewmodels.request.ChangePasswordRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.CustomerResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class CustomerController {
    private final CustomerService userService;
    private final CustomerMapper userMapper;

    public CustomerController(CustomerService userService) {
        this.userService = userService;
        this.userMapper = Mappers.getMapper(CustomerMapper.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerAPIResponse<CustomerResponse>> getUserInfo(@PathVariable String id) {
        Optional<Customer> user = userService.findById(id);

        if (user.isPresent()) {
            CustomerResponse userResponse = userMapper.toResponse(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(ServerAPIResponse.success(userResponse));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ServerAPIResponse.error("User not found"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ServerAPIResponse<String>> changePassword(
            @AuthenticationPrincipal JwtUserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUserId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ServerAPIResponse.success("Đổi mật khẩu thành công."));
    }
}
