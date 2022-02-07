package com.zero.controller;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author Zero
 * @date 2022/2/4 22:43
 * @description
 * @since 1.8
 **/
@RestController
public class TestController {

    @GetMapping("/h")
    public String test() {
        return "test success";
    }


    /**
     * 获取当前系统的限流策略
     * @return
     */
    @GetMapping("/gw/flow/rules")
    public Set<GatewayFlowRule> getRules() {
        return GatewayRuleManager.getRules();
    }
    /**
     * 获取我定义的api分组
     */
    @GetMapping("gw/api/groups")
    public Set<ApiDefinition> getApi() {
        return GatewayApiDefinitionManager.getApiDefinitions();
    }
}
