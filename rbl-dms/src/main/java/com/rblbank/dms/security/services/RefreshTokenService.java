package com.rblbank.dms.security.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rblbank.dms.exception.TokenRefreshException;
import com.rblbank.dms.models.RefreshToken;
import com.rblbank.dms.models.User;
import com.rblbank.dms.repository.RefreshTokenRepository;
import com.rblbank.dms.repository.UserRepository;

@Service
public class RefreshTokenService {
  @Value("${dms.app.jwtRefreshExpirationMs}")
  private Long refreshTokenDurationMs;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken createRefreshToken(String username) {
    RefreshToken refreshToken = new RefreshToken();

    refreshToken.setUser(userRepository.findByUsername(username).get());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
    refreshToken.setToken(UUID.randomUUID().toString());

    refreshToken = refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
    }

    return token;
  }

	/*
	 * @Transactional public int deleteByUserId(Long userId) { return
	 * refreshTokenRepository.deleteByUser(userRepository.findById(userId).get()); }
	 */
}
