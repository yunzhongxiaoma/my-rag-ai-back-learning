# JWT校验为null问题排查指南

## 🔍 问题现象
- 后端日志显示：`jwt校验:null`
- 前端可能收到401未授权响应
- 用户无法正常访问需要认证的接口

## 🎯 已修复的问题

### 1. JWT拦截器增强
- ✅ 添加了对 `Authorization` 头的兼容支持
- ✅ 增加了token空值检查
- ✅ 添加了userId空值安全检查
- ✅ 添加了ThreadLocal清理机制

### 2. 配置优化
- ✅ 统一token头名称为 `Authorization`
- ✅ 保持前后端一致性

## 🧪 测试步骤

### 1. 检查应用启动状态
```bash
# 检查应用是否正常启动
curl http://localhost:8989/actuator/health

# 检查端口是否监听
netstat -tlnp | grep 8989
```

### 2. 测试登录接口
```bash
# 测试登录
curl -X POST "http://localhost:8989/api/v1/user/login?userName=admin&password=123456"

# 预期响应格式：
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "userName": "admin",
    "name": "管理员",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 3. 测试JWT认证
```bash
# 使用获取的token测试认证接口
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" "http://localhost:8989/api/v1/user/info"
```

### 4. 前端调试
打开浏览器开发者工具：
1. **检查localStorage**：确认token已正确存储
2. **检查Network请求**：确认Authorization头已正确发送
3. **检查Console日志**：查看前端调试信息

## 🔧 常见问题和解决方案

### 问题1：Token为null
**原因**：
- 前端未正确存储token
- 请求头名称不匹配
- token被浏览器或代理服务器过滤

**解决方案**：
```javascript
// 前端检查token存储
console.log('Token:', localStorage.getItem('token'));

// 检查请求头设置
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  console.log('发送请求，token:', token);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 问题2：Token格式错误
**原因**：
- 缺少"Bearer "前缀
- token包含特殊字符
- token被截断

**解决方案**：
- 确保前端发送格式：`Authorization: Bearer {token}`
- 检查token完整性

### 问题3：JWT解析失败
**原因**：
- 秘钥不匹配
- token已过期
- token签名无效

**解决方案**：
```yaml
# 检查application.yml配置
kinghy:
  jwt:
    user-secret-key: kinghy  # 确保与生成token时一致
    user-ttl: 7200000       # 2小时过期时间
    user-token-name: Authorization
```

### 问题4：Claims中userId为null
**原因**：
- JWT生成时未设置userId
- 常量名称不匹配

**解决方案**：
```java
// 检查登录时token生成
Map<String, Object> claims = new HashMap<>();
claims.put(JwtClaimsConstant.USER_ID, user.getId()); // 确保userId不为null

// 检查常量定义
public static final String USER_ID = "userId";
```

## 📊 监控和日志

### 关键日志位置
- **应用日志**：`logs/application.log`
- **错误日志**：`logs/error.log`
- **JWT拦截器日志**：搜索 `jwt校验` 关键字

### 重要日志信息
```
# 正常情况
jwt校验:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImV4cCI6MTcwNDYyODgxNX0...
当前用户的id：1

# 异常情况
jwt校验:null
JWT中userId为null
令牌已过期
校验jwt异常
```

## 🚀 验证修复结果

### 1. 重启应用
```bash
# 停止应用
pkill -f my-rag-ai-back-learning

# 启动应用
java -jar target/my-rag-ai-back-learning-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 2. 完整测试流程
1. 用户登录 → 获取token
2. 使用token访问受保护接口 → 验证认证成功
3. 不带token访问 → 验证返回401
4. 使用过期/无效token → 验证返回401

### 3. 前端测试
1. 登录系统
2. 查看浏览器控制台，确认无JWT相关错误
3. 正常使用系统功能

## 📞 如果问题仍然存在

1. **收集日志**：提供完整的错误日志和请求响应信息
2. **检查环境**：确认JDK版本、依赖版本等
3. **网络检查**：确认前后端网络连通性
4. **配置检查**：对比配置文件与预期配置

## 🔄 后续优化建议

1. **添加JWT刷新机制**：避免token过期导致的用户体验问题
2. **增强安全性**：添加IP白名单、请求频率限制等
3. **完善监控**：添加JWT相关的监控指标和告警
4. **单元测试**：为JWT相关功能添加完整的单元测试

---

**最后更新**：2024-01-07  
**问题状态**：已修复  
**测试状态**：待验证