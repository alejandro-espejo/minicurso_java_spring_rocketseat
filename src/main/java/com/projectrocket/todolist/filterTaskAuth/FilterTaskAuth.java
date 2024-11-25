package com.projectrocket.todolist.filterTaskAuth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.projectrocket.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository iUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();
        if (servletPath.startsWith("/tasks/")) {
            // Obtain the authorization (user and password)
            var authorization = request.getHeader("Authorization");
            var authEncoded = authorization.substring(6); // Remove unnecessary information.
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecode);
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];
            System.out.println(username + " - " + password);

            // Valid user
            var userModel = iUserRepository.findByUsername(username);
            if (userModel == null) {
                response.sendError(401);
            } else {
                // valid password
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), userModel.getPassword());
                if (passwordVerify.verified) {
                    request.setAttribute("idUser", userModel.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
