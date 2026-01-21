package com.sixgroup.refit.ejemplo.config;

import com.sixgroup.refit.ejemplo.service.IpLockService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IpFilter extends OncePerRequestFilter {

    private final IpLockService ipLockService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        if (ipLockService.isIpBlocked(ip)) {
            response.setStatus(423); // Locked
            response.getWriter().write("Tu IP esta bloqueada temporalmente por actividad sospechosa.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}