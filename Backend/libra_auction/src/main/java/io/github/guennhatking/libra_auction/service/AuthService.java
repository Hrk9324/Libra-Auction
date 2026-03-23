package io.github.guennhatking.libra_auction.service;

import io.github.guennhatking.libra_auction.repos.TaiKhoanPasswordRepository;
import io.github.guennhatking.libra_auction.security.JwtTokenProvider;

import org.postgresql.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService<passwordEncoder> {
    @Autowired
    private TaiKhoanPasswordRepository taiKhoanPasswordRepository;
    @Autowired
    private PasswordUtil passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;

    public String authenticate(String username, String password) {
        TaiKhoanPasswordRepository user = taiKhoanPasswordRepository.findByUsername(username);
        
        return null;
    }
}