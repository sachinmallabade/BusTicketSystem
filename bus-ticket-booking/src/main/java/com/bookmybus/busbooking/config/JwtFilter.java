package com.bookmybus.busbooking.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bookmybus.busbooking.service.JWTService;
import com.bookmybus.busbooking.service.UserAuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{
	
	@Autowired
	private JWTService jwtService;
	
	@Autowired
	ApplicationContext context;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		String token = null;
		String userName = null;
		
		 if (authHeader != null && authHeader.startsWith("Bearer ")) {
	            token = authHeader.substring(7);  // Corrected token extraction
	            try {
	                userName = jwtService.extractUserName(token);
	            } catch (Exception e) {
	                // Log the exception and handle invalid token
	                System.out.println("Invalid JWT Token: " + e.getMessage());
	            }
	        }
		
		if(userName != null && SecurityContextHolder.getContext().getAuthentication() == null)
		{
			
			UserDetails userDetails = context.getBean(UserAuthService.class).loadUserByUsername(userName);
			if(jwtService.validateToken(token,userDetails))
			{
				UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
				authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authtoken);
			}
		}
		
		filterChain.doFilter(request, response);
	}

}
