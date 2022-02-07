package com.zero.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Zero
 * @date 2022/2/7 17:48
 * @description
 * @since 1.8
 **/
@RestController
public class UserInfoController {


    @GetMapping("/user/info")
    public Principal userInfo(Principal principal) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return principal;
    }
}
