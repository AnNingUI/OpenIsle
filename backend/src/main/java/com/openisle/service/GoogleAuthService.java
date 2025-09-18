package com.openisle.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.openisle.model.Role;
import com.openisle.model.User;
import com.openisle.repository.UserRepository;
import com.openisle.service.AvatarGenerator;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

  private final UserRepository userRepository;
  private final AvatarGenerator avatarGenerator;

  @Value("${google.client-id:}")
  private String clientId;

  public Optional<AuthResult> authenticate(
    String idTokenString,
    com.openisle.model.RegisterMode mode,
    boolean viaInvite
  ) {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
      new NetHttpTransport(),
      new JacksonFactory()
    )
      .setAudience(Collections.singletonList(clientId))
      .build();
    try {
      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken == null) {
        return Optional.empty();
      }
      GoogleIdToken.Payload payload = idToken.getPayload();
      String email = payload.getEmail();
      String name = (String) payload.get("name");
      String picture = (String) payload.get("picture");
      return Optional.of(processUser(email, name, picture, mode, viaInvite));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private AuthResult processUser(
    String email,
    String name,
    String avatar,
    com.openisle.model.RegisterMode mode,
    boolean viaInvite
  ) {
    Optional<User> existing = userRepository.findByEmail(email);
    if (existing.isPresent()) {
      User user = existing.get();
      if (!user.isVerified()) {
        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
      }
      return new AuthResult(user, false);
    }
    User user = new User();
    String baseUsername = email.split("@")[0];
    String username = baseUsername;
    int suffix = 1;
    while (userRepository.findByUsername(username).isPresent()) {
      username = baseUsername + suffix++;
    }
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword("");
    user.setRole(Role.USER);
    user.setVerified(true);
    user.setApproved(mode == com.openisle.model.RegisterMode.DIRECT || viaInvite);
    if (avatar != null) {
      user.setAvatar(avatar);
    } else {
      user.setAvatar(avatarGenerator.generate(username));
    }
    return new AuthResult(userRepository.save(user), true);
  }
}
