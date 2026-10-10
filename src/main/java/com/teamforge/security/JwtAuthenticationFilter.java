package com.teamforge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomUserDetailsService uds;

    public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService uds) { this.jwt = jwt; this.uds = uds; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwt.extractEmail(h.substring(7));
                UserDetails ud = uds.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken t = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                t.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(t);
            } catch (Exception ignored) {
                // invalid or expired token: request continues unauthenticated -> 401
            }
        }
        chain.doFilter(req, res);
    }
}
