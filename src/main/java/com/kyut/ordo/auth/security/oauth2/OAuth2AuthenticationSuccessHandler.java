package com.kyut.ordo.auth.security.oauth2;

import com.kyut.ordo.auth.provider.AuthProvider;
import com.kyut.ordo.auth.security.jwt.JwtService;
import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        
        OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
        String email = extractEmail(oauth2User);
        
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(oauth2User));
        
        Authentication customAuth = new UsernamePasswordAuthenticationToken(
                user, 
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        String jwt = jwtService.generateToken(customAuth);
        
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?token=" + jwt);
    }
    
    private String extractEmail(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        return (String) attributes.get("email");
    }
    
    private UserEntity createUser(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        UserEntity user = new UserEntity();
        user.setName((String) attributes.getOrDefault("name", "User"));
        user.setEmail((String) attributes.get("email"));
        user.setImageUrl((String) attributes.getOrDefault("picture", null));
        user.setProvider(AuthProvider.GOOGLE);
        
        return userRepository.save(user);
    }
}
