package com.kinghy.rag.config;

import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.JwtTokenUserInterceptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author aliang
 * @date 2025/2/8
 */

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * ETL中的DocumentTransformer的实现，将文本数据源转换为多个分割段落
     * @return
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    ChatClient chatclient(ChatClient.Builder builder){
        return builder.defaultSystem("你是一个乐于助人解决问题的AI机器人")
                .build();
    }


    /**
     * 注册拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户JWT拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(ApplicationConstant.API_VERSION+"/user/login") // 排除用户登录接口拦截所有请求
                .excludePathPatterns(ApplicationConstant.API_VERSION+"/user/register") // 排除用户注册接口
                .excludePathPatterns(ApplicationConstant.API_VERSION+"/test/**") // 排除测试接口
                .excludePathPatterns("/error") // 排除错误页面
                .excludePathPatterns("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"); // 排除Swagger相关路径
    }

    /**
     * 配置HTTP消息转换器，确保UTF-8编码
     */
    @Override
    public void configureMessageConverters(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        converters.add(0, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
    }
}