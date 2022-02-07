package com.zero.service.impl;

import com.zero.constant.LoginConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zero
 * @date 2022/2/7 20:35
 * @description
 * @since 1.8
 **/
@Service("UserDetail")
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final String loginType = requestAttributes.getRequest().getParameter("login_type");
        if(StringUtils.isEmpty(loginType)) {
            throw new AuthenticationServiceException("登录类型不能为空");
        }
        UserDetails userDetails = null;
        try {
            //如果是根据token刷新token，那么需要纠正username（将获取刷新令牌时注入的username为用户id）
            String grantType = requestAttributes.getRequest().getParameter("grant_type");
            if(grantType .equals("refresh_token")) {
                username = adjustUsername(username, loginType);
            }
            switch (loginType) {
                case LoginConstant.ADMIN_TYPE:
                    userDetails = loadSysUserByUsername(username);
                    break;
                case LoginConstant.MEMBER_TYPE:
                    userDetails = loadMemUserByUsername(username);
                    break;
                default:
                    throw new AuthenticationServiceException("暂不支持的登录方式：" + loginType);
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new UsernameNotFoundException("用户名：" + username + "不存在");
        }
        return userDetails;
    }

    /**
     * 纠正用户名
     * @param username
     * @return
     */
    private String adjustUsername(String username, String loginType) {
        if(loginType.equals(LoginConstant.ADMIN_TYPE)) {

            return jdbcTemplate.queryForObject(LoginConstant.QUERY_ADMIN_USER_WITH_ID, String.class, username);
        }
        if(loginType.equals(LoginConstant.MEMBER_TYPE)) {
            return jdbcTemplate.queryForObject(LoginConstant.QUERY_MEMBER_USER_WITH_ID, String.class, username);
        }
        return username;
    }

    /**
     * 查询前台会员
     * @param username
     * @return
     */
    private UserDetails loadMemUserByUsername(String username) {
        return jdbcTemplate.queryForObject(LoginConstant.QUERY_MEMBER_SQL, new RowMapper<User>() {

            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                if(resultSet.wasNull()) {
                    throw new UsernameNotFoundException("用户名：" + username + "不存在");
                }
                final long id = resultSet.getLong("id");
                final String password = resultSet.getString("password");
                final int status = resultSet.getInt("status");
                return new User(String.valueOf(id), password, status == 1, true,true,true, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
            }
        }, username, username);

    }

    /**
     * 查询后台用户相关信息
     * @param username
     * @return
     */
    private UserDetails loadSysUserByUsername(String username) {
        //查出用户
        return jdbcTemplate.queryForObject(LoginConstant.QUERY_ADMIN_SQL, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                if(resultSet.wasNull()) {
                    throw new UsernameNotFoundException("用户名：" + username + "不存在");
                }
                final long id = resultSet.getLong("id");
                final String password = resultSet.getString("password");
                final int status = resultSet.getInt("status");
                return new User(String.valueOf(id), password, status == 1, true,true,true, getSysUserPermission(id));
            }
        }, username);
    }

    /**
     * 根据后台用户的id查询用户的权限数据
     * @param id
     * @return
     */
    private Collection<? extends GrantedAuthority> getSysUserPermission(long id) {
        //超级管理员为所有权限
        List<String> permissions = null;
        String roleCode = jdbcTemplate.queryForObject(LoginConstant.QUERY_ROLE_CODE_SQL, String.class, id);
        if("ROLE_ADMIN".equalsIgnoreCase(roleCode)) {
            permissions = jdbcTemplate.queryForList(LoginConstant.QUERY_ALL_PERMISSIONS, String.class);
        } else { //普通用户
            permissions = jdbcTemplate.queryForList(LoginConstant.QUERY_PERMISSION_SQL, String.class, id);
        }
        if(permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .distinct()
                .map(perm -> new SimpleGrantedAuthority(perm))
                .collect(Collectors.toSet());
    }
}
