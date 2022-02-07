package com.zero.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * @author Zero
 * @date 2022/2/7 15:35
 * @description
 * @since 1.8
 **/
@EnableAuthorizationServer //开启授权服务器的功能
@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("UserDetail")
    private UserDetailsService userDetailsService;

    @Autowired
    @Qualifier("jwtTokenStore")
    private TokenStore jwtTokenStore;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    /**
     * 添加第三方客户端
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("coin-api") //客户端名称
                .secret(passwordEncoder.encode("coin-secret"))
                .scopes("all") //授权范围
                .authorizedGrantTypes("password", "refresh_token", "implicit", "authorization_code")
                .accessTokenValiditySeconds(3600 * 24 * 7)
                .refreshTokenValiditySeconds(3600 * 24 * 30) //刷新token的有效期
                .and()
                //内部服务调用获取临时的token
                .withClient("inside-app")
                .secret(passwordEncoder.encode("inside-secret"))
                .authorizedGrantTypes("client_credentials")
                .scopes("all")
                .accessTokenValiditySeconds(7 * 24 * 3600);
    }

    /**
     * 配置验证管理器，UserDetailService
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .tokenStore(jwtTokenStore)
                .tokenEnhancer(jwtAccessTokenConverter);
        super.configure(endpoints);
    }
}
