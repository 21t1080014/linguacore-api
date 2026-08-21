package com.nomnom.linguacore.security;

import com.nomnom.linguacore.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    public JwtAuthFilter (JwtService jwtService){
        this.jwtService = jwtService;
    }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response ,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // Không có token → đi tiếp, SecurityConfig sẽ quyết định chặn hay không
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);   // cắt bỏ "Bearer "

        try {
            String email = jwtService.extractEmail(token);

            // Chưa có ai được xác thực trong request này → đặt danh tính vào
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, List.of());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Token giả/hết hạn → không đặt danh tính, request đi tiếp như khách vãng lai
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
