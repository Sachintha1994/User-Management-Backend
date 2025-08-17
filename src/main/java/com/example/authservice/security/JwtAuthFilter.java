package com.example.authservice.security;

import com.example.authservice.domain.user.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository users;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc) throws ServletException, IOException {

        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h == null || !h.startsWith("Bearer ")) {
            fc.doFilter(req, res);
            return;
        }
        String token = h.substring(7);
        String username;
        try {
            username = jwt.extractUsername(token);
        } catch (Exception e) {
            fc.doFilter(req, res);
            return;
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var uOpt = users.findByEmail(username);
            if (uOpt.isPresent() && jwt.isTokenValid(token)) {
                UserDetails u = uOpt.get();
                var auth = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        fc.doFilter(req, res);
    }
}
