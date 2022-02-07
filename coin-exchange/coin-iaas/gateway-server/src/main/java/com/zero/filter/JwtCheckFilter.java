package com.zero.filter;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Zero
 * @date 2022/2/7 18:36
 * @description 网关进行拦截，看是否
 * @since 1.8
 **/
@Component
public class JwtCheckFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Set<String> noRequireTokenUris = new HashSet<String>(){{add("/admin/login");}};

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //该接口是否需要token访问
        if(!isRequireToken(exchange)) {
            return chain.filter(exchange); //不需要token，直接放行
        }
        //取出用户的token
        String token = getUserToken(exchange);
        //判断用户的token是否有效
        if (StringUtils.isEmpty(token)) {
            return buildNotAuthorizationResult(exchange);
        }
        final Boolean aBoolean = redisTemplate.hasKey(token);
        if(aBoolean != null && aBoolean) {
            return chain.filter(exchange);
        }
        return buildNotAuthorizationResult(exchange);
    }

    /**
     * 给用户响应一个没有token的错误
     * @param exchange
     * @return
     */
    private Mono<Void> buildNotAuthorizationResult(ServerWebExchange exchange) {
        final ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set("Content-Type", "application/json");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", "NoAuthorization");
        jsonObject.put("errorMsg", "token is Null or Error");
        final DataBuffer wrap = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(wrap));
    }


    private String getUserToken(ServerWebExchange exchange) {
        final String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return token == null ? null : token.replace("bearer ", "");
    }

    private boolean isRequireToken(ServerWebExchange exchange) {
        final String path = exchange.getRequest().getURI().getPath();
        if(noRequireTokenUris.contains(path)) {
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
