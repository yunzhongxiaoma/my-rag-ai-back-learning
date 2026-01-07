# JWT Token为空问题解决方案

## 🎯 问题描述

**错误信息**: `JWT token为空，请求路径: /api/v1/chat/stream`

**问题现象**:
- 用户在聊天页面发送消息时，后端收到JWT token为空的警告
- 前端可能收到401未授权响应
- 聊天功能无法正常使用

## 🔍 根本原因分析

经过深入分析，发现问题的根本原因是：

### 1. 前端API调用缺少JWT认证
- `ChatApi.ts` 中的 `sendChatMessageApi` 和 `sendRagChatMessageApi` 函数直接使用 `fetch` API
- **没有包含JWT token**在请求头中
- 导致后端JWT拦截器收到空token

### 2. StreamApi也存在同样问题
- `StreamApi.ts` 中的流式接口调用也缺少JWT认证头
- 虽然当前聊天页面没有使用，但潜在问题存在

### 3. 缺少统一的HTTP客户端
- 前端存在多种HTTP调用方式：axios拦截器、直接fetch调用
- 没有统一的JWT认证处理机制

## ✅ 解决方案

### 1. 修复ChatApi.ts
```typescript
// 修复前：直接使用fetch，没有认证头
export const sendChatMessageApi = async (message: string): Promise<Response> => {
  return fetch(`${service.defaults.baseURL}${ChatApi.Chat}?message=${encodeURIComponent(message)}`);
};

// 修复后：使用统一的HTTP客户端，自动包含JWT认证
export const sendChatMessageApi = async (message: string): Promise<Response> => {
  return httpClient.get(ChatApi.Chat, { message });
};
```

### 2. 修复StreamApi.ts
```typescript
// 添加统一的认证头获取函数
const getAuthHeaders = (): Record<string, string> => {
  const token = localStorage.getItem('token');
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  
  return headers;
};

// 在fetchEventSource中使用认证头
fetchEventSource(BaseUrl + "/chat/stream?message=" + message, {
  method: "GET",
  headers: getAuthHeaders(), // 使用认证头
  // ... 其他配置
});
```

### 3. 创建统一的HTTP客户端
创建了 `httpClient.ts` 工具类：
- 自动处理JWT token的添加
- 统一的错误处理
- 支持GET、POST、PUT、DELETE等方法
- 自动处理URL参数编码

### 4. 增强JWT拦截器
后端JWT拦截器已经增强：
- 兼容 `Authorization` 和 `authorization` 头
- 添加了token空值检查
- 添加了userId空值安全检查
- 添加了ThreadLocal清理机制

## 🧪 测试验证

### 1. 创建了测试工具
- `authTest.ts`: JWT认证测试工具
- 可在浏览器控制台调用 `testJWT()` 进行测试

### 2. 测试步骤
1. 用户登录获取token
2. 调用聊天接口发送消息
3. 检查后端日志，确认不再出现"JWT token为空"
4. 验证聊天功能正常工作

### 3. 验证清单
- [ ] 用户可以正常登录
- [ ] 聊天页面可以发送消息
- [ ] 后端日志显示正确的JWT token
- [ ] 不再出现401未授权错误

## 📋 修改文件清单

### 前端修改
1. **my-rag-ai-front-learning/src/api/ChatApi.ts** - 修复聊天API的JWT认证
2. **my-rag-ai-front-learning/src/api/StreamApi.ts** - 修复流式API的JWT认证
3. **my-rag-ai-front-learning/src/utils/httpClient.ts** - 新增统一HTTP客户端
4. **my-rag-ai-front-learning/src/utils/authTest.ts** - 新增JWT测试工具
5. **my-rag-ai-front-learning/src/main.ts** - 集成测试工具

### 后端修改
1. **my-rag-ai-back-learning/src/main/java/com/kinghy/rag/common/JwtTokenUserInterceptor.java** - 增强JWT拦截器
2. **my-rag-ai-back-learning/src/main/resources/application.yml** - 统一token头名称

## 🚀 部署和验证

### 1. 重启服务
```bash
# 前端
cd my-rag-ai-front-learning
npm run dev

# 后端
cd my-rag-ai-back-learning
mvn spring-boot:run
```

### 2. 功能测试
1. 打开浏览器访问前端应用
2. 登录系统
3. 进入聊天页面
4. 发送测试消息
5. 检查是否正常收到回复

### 3. 日志验证
检查后端日志，应该看到：
```
jwt校验:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImV4cCI6MTcwNDYyODgxNX0...
当前用户的id：1
```

而不是：
```
JWT token为空，请求路径: /api/v1/chat/stream
```

## 🔧 预防措施

### 1. 代码规范
- 所有API调用都应该使用统一的HTTP客户端
- 避免直接使用fetch API进行认证接口调用
- 新增API时要考虑JWT认证需求

### 2. 测试覆盖
- 为JWT认证相关功能添加单元测试
- 集成测试要包含认证场景
- 定期进行端到端测试

### 3. 监控告警
- 监控401认证失败的频率
- 设置JWT相关错误的告警
- 定期检查认证相关日志

## 📞 后续支持

如果问题仍然存在：
1. 检查浏览器控制台是否有JavaScript错误
2. 检查Network面板中的请求头是否包含Authorization
3. 检查后端日志中的详细错误信息
4. 使用 `testJWT()` 函数进行诊断

---

**解决时间**: 2024-01-07  
**问题状态**: ✅ 已解决  
**测试状态**: 待验证  
**影响范围**: 聊天功能、流式接口