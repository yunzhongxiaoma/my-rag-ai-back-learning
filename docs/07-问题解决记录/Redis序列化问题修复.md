# Redis 序列化问题修复记录

## 问题描述

**时间**: 2026-01-09  
**问题**: Redis 缓存操作时出现 `ClassCastException` 错误

### 错误信息
```
java.lang.ClassCastException: class java.util.ArrayList cannot be cast to class java.lang.String 
(java.util.ArrayList and java.lang.String are in module java.base of loader 'bootstrap')
at org.springframework.data.redis.serializer.StringRedisSerializer.serialize(StringRedisSerializer.java:36)
at com.kinghy.rag.service.impl.ChatCacheServiceImpl.cacheRecentMessages(ChatCacheServiceImpl.java:131)
```

### 问题分析

1. **根本原因**: `RedisConfig` 中配置了 `StringRedisSerializer` 作为值序列化器，但代码中试图存储复杂对象（如 `List<ChatMessage>`、`ChatSession` 等）
2. **序列化器不匹配**: `StringRedisSerializer` 只能处理字符串类型，无法序列化复杂对象
3. **时间类型支持**: 实体类中使用了 `LocalDateTime`，需要 Jackson 的 JSR310 模块支持

## 解决方案

### 1. 更新 Redis 配置

**文件**: `src/main/java/com/kinghy/rag/config/RedisConfig.java`

**修改前**:
```java
// 使用 StringRedisSerializer 序列化值（问题所在）
template.setValueSerializer(new StringRedisSerializer());
template.setHashValueSerializer(new StringRedisSerializer());
```

**修改后**:
```java
// 创建支持 Java 8 时间类型和类型信息的 ObjectMapper
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
// 启用默认类型信息，以便正确反序列化复杂对象
objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

// 使用配置好的 ObjectMapper 创建 JSON 序列化器
GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
template.setValueSerializer(jsonSerializer);
template.setHashValueSerializer(jsonSerializer);
```

### 2. 实体类序列化支持

**文件**: 
- `src/main/java/com/kinghy/rag/entity/ChatMessage.java`
- `src/main/java/com/kinghy/rag/entity/ChatSession.java`

**修改**: 添加 `Serializable` 接口支持
```java
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    // ... 其他代码
}
```

### 3. 配置特性

- **键序列化器**: 继续使用 `StringRedisSerializer`（键保持字符串格式）
- **值序列化器**: 使用 `GenericJackson2JsonRedisSerializer`（支持复杂对象）
- **时间支持**: 注册 `JavaTimeModule` 支持 `LocalDateTime` 等 Java 8 时间类型
- **类型信息**: 启用 `DefaultTyping.NON_FINAL` 保留类型信息，确保反序列化时类型正确

## 测试验证

### 创建测试类
**文件**: `src/test/java/com/kinghy/rag/config/RedisSerializationTest.java`

### 测试覆盖
1. `ChatMessage` 对象序列化/反序列化
2. `ChatSession` 对象序列化/反序列化  
3. `List<ChatMessage>` 集合序列化/反序列化
4. 字符串序列化向后兼容性
5. 序列化器配置正确性

### 测试结果
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

## 影响范围

### 受益的功能模块
1. **聊天缓存服务** (`ChatCacheServiceImpl`)
   - `cacheRecentMessages()` - 缓存最近消息列表
   - `cacheSessionInfo()` - 缓存会话信息
   - `cacheUserSessions()` - 缓存用户会话列表

2. **其他使用 Redis 的服务**
   - 所有需要缓存复杂对象的场景
   - 现有的字符串缓存功能保持兼容

### 性能优化
- 避免了手动 JSON 序列化/反序列化的开销
- 统一的序列化配置，减少代码重复
- 支持类型信息保留，避免类型转换错误

## 注意事项

### 1. 向后兼容性
- 现有的字符串缓存数据仍然可以正常读取
- `WordFrequencyController` 中的手动 JSON 处理方式仍然有效

### 2. 存储格式变化
- 新的缓存数据将包含类型信息（JSON 格式）
- 数据体积可能略有增加，但换来了类型安全

### 3. 配置要求
- 需要 Jackson JSR310 模块支持（项目中已包含）
- 实体类需要实现 `Serializable` 接口（已添加）

## 后续建议

1. **监控缓存性能**: 观察新配置下的缓存读写性能
2. **清理旧缓存**: 考虑在部署时清理可能存在的旧格式缓存数据
3. **统一序列化策略**: 其他需要序列化的场景可以复用此配置

## 修复验证

### 编译验证
```bash
mvn clean compile
# BUILD SUCCESS
```

### 测试验证
```bash
mvn test -Dtest=RedisSerializationTest
# Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

### 功能验证
- 聊天缓存服务的 `cacheRecentMessages` 方法不再抛出 `ClassCastException`
- 复杂对象可以正确存储和读取
- 时间类型字段正确序列化

---

**修复人员**: yunzhongxiaoma  
**修复时间**: 2026-01-09  
**状态**: 已完成并验证