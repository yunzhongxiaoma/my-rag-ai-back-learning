# RAG AI 知识库系统

欢迎使用RAG AI知识库系统接口文档！

## 系统简介

这是一个基于Spring Boot 3.x和Spring AI构建的智能知识库系统，集成了以下核心功能：

- **智能问答**：基于RAG（检索增强生成）技术的AI对话
- **知识管理**：支持多种格式文档的上传、存储和检索
- **用户系统**：完整的用户认证和权限管理
- **内容安全**：敏感词过滤和内容审核
- **系统监控**：完善的日志记录和系统监控

## 快速开始

### 1. 用户登录
首先调用登录接口获取JWT Token：
```
POST /api/v1/user/login
```

### 2. 设置认证头
在后续请求中添加认证头：
```
Authorization: Bearer <your-jwt-token>
```

### 3. 上传知识库文件
```
POST /api/v1/knowledge/file/upload
```

### 4. 开始AI对话
```
POST /api/v1/ai/chat
```

## 技术栈

- **后端框架**：Spring Boot 3.4.2
- **AI框架**：Spring AI 1.0.0
- **数据库**：MySQL + MyBatis Plus
- **缓存**：Redis
- **向量数据库**：Milvus
- **文档处理**：Apache Tika
- **API文档**：Knife4j + OpenAPI 3

## 联系我们

如有问题，请联系开发团队。

---
*Powered by Spring AI & Knife4j*