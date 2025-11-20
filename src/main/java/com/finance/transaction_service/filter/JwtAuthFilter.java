package com.finance.transaction_service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.transaction_service.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            System.out.println("doFilterInternal");
            String authHeader = request.getHeader("Authorization");
            System.out.println("authHeader=== " + authHeader);
            if (authHeader == null || !authHeader.contains("Bearer")) {
                System.out.println("authHeader=====");
                throw new JwtException("Invalid Token");
            }

            String token = authHeader.substring(7);
            String userName = request.getHeader("userName");
            String role = jwtService.extractRole(token);
            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.validateToken(token, userName)) {
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userName, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    throw new JwtException("Unauthorized user");
                }
            }
            throw new JwtException("Unauthorized user");
        } catch (ExpiredJwtException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    Map.of("status", HttpStatus.UNAUTHORIZED.value(),
                            "message", "JWT token has expired")
            ));
        } catch (JwtException | IllegalArgumentException ex) {
            String error = ex.getMessage();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    Map.of("status", HttpStatus.BAD_REQUEST.value(),
                            "message", (error != null || error!="") ? "Invalid JWT token" : error)
            ));
        } catch (Exception e){
            String error = e.getMessage();
            System.out.println(error);
        }
    }
}
