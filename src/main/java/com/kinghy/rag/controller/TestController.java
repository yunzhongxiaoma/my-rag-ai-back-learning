package com.kinghy.rag.controller;

import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器
 * 用于验证CORS配置和基本功能
 * 
 * @author yunzhongxiaoma
 */
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/test")
@Tag(name = "TestController", description = "测试接口")
public class TestController {

    /**
     * 测试接口 - 不需要认证
     */
    @GetMapping("/ping")
    @Operation(summary = "ping", description = "测试接口连通性")
    public BaseResponse<String> ping() {
        return ResultUtils.success("pong");
    }

    /**
     * 测试CORS预检请求
     */
    @RequestMapping(value = "/cors", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @Operation(summary = "cors", description = "测试CORS配置")
    public BaseResponse<String> testCors() {
        return ResultUtils.success("CORS test successful");
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "health", description = "应用健康检查")
    public BaseResponse<String> health() {
        return ResultUtils.success("Application is running");
    }
}