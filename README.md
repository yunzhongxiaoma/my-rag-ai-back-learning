# RAG AI 后端学习项目

> 本仓库是基于 [kinghy-rag-ai-back](https://github.com/kinghy949/kinghy-rag-ai-back) 的复刻学习实践仓库，非原项目作者，仅用于个人技术学习和架构分析。

## 📋 项目简介

基于Spring AI构建的RAG（检索增强生成）智能问答系统后端，主要特色是将原项目的PostgreSQL向量存储方案迁移至Milvus向量数据库，提升向量检索性能和系统扩展性。

## 🛠️ 技术栈

### 核心框架
- **Spring Boot**: 3.4.2
- **JDK**: 17
- **Maven**: 3.9.9
- **Spring AI**: 1.0.0
- **Spring AI Alibaba**: 1.0.0.1

### 数据存储
- **MySQL**: 8.0+ (业务数据存储)
- **Milvus**: 2.5+ (向量数据库，替代原PostgreSQL+vector方案)
- **Redis**: 6.0+ (缓存服务)
- **阿里云OSS**: 对象存储服务

### AI服务
- **阿里云通义千问**: 大语言模型服务
- **文档解析**: Apache Tika, PDF处理
- **中文分词**: IK Analyzer

## 🚀 快速开始

### 1. 环境准备

#### 1.1 基础环境
- JDK 17+
- Maven 3.9+
- Docker & Docker Compose（推荐）

#### 1.2 启动数据库服务
```bash
# 启动Milvus和相关服务
docker-compose -f docs/05-部署与启动/docker-milvus-attu-compose.yml up -d

# 检查服务状态
docker-compose -f docs/05-部署与启动/docker-milvus-attu-compose.yml ps
```

#### 1.3 初始化MySQL数据库
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE my_rag CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行初始化脚本
mysql -u root -p my_rag < src/main/resources/sql/init.sql
```

### 2. 配置设置

#### 2.1 阿里云服务配置
1. **通义千问API**: 
   - 访问 [DashScope控制台](https://dashscope.console.aliyun.com/)
   - 创建API Key并设置到环境变量 `DASHSCOPE_API_KEY`

2. **阿里云OSS**:
   - 访问 [OSS控制台](https://oss.console.aliyun.com/)
   - 创建OSS Bucket并配置Access Key

#### 2.2 配置文件
根据你的环境修改 `src/main/resources/application-dev.yml`：
```yaml
spring:
  ai:
    dashscope:
      api-key: your_actual_api_key_here
  
  datasource:
    password: your_db_password
    
aliyun:
  alioss:
    access-key-id: your_oss_access_key_id
    access-key-secret: your_oss_access_key_secret
```

### 3. 启动应用
```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用提供的启动脚本（Windows）
start-dev.bat
```

### 4. 验证部署
- **应用健康检查**: http://localhost:8989/actuator/health
- **Attu管理界面**: http://localhost:3000 (Milvus可视化管理)
- **默认登录账户**: admin/admin

## 🎯 RAG功能特性
### ✅ 已实现功能
#### 核心RAG功能
- [x] **📄 文档处理**
  - [x] 支持多格式文档上传（PDF、DOC、DOCX、TXT）
  - [x] 基于Apache Tika的文档解析
  - [x] 智能文档分块处理
  - [x] 文档元数据提取和管理

- [x] **🔍 向量化与检索**
  - [x] 文档内容向量化（基于通义千问embedding）
  - [x] Milvus向量数据库存储（替代PostgreSQL+pgvector）
  - [x] 高效的向量相似度检索
  - [x] 支持COSINE相似度计算

- [x] **💬 智能问答**
  - [x] 基于检索增强生成（RAG）的问答系统
  - [x] 流式响应支持（Server-Sent Events）
  - [x] 对话历史记忆功能
  - [ ] 上下文感知的多轮对话

- [x] **🛡️ 内容安全**
  - [x] 敏感词检测和过滤
  - [x] 可配置的敏感词分类管理
  - [x] 实时内容审核

- [x] **👤 用户管理**
  - [x] JWT身份认证
  - [x] 基于角色的权限控制
  - [x] 用户会话管理

- [x] **📊 系统监控**
  - [x] 操作日志记录
  - [x] 词频统计分析
  - [x] 系统健康检查

## 🚧 待实现功能

### 高级RAG功能
- [ ] **📑 分块预览**
- [ ] **📝 反馈信息收集**
- [ ] **🔄 混合检索**
  - [ ] 向量检索 + 关键词检索
  - [ ] 多路召回策略
  - [ ] 检索结果重排序（Rerank）
  - [ ] 检索策略自适应选择

### 扩展功能
- [ ] **🌐 多模态支持**
  - [ ] 图片文档处理
  - [ ] 表格数据提取
  - [ ] 音频转文本


## 🔄 主要改进

### 向量数据库迁移
- **原方案**: PostgreSQL + pgvector扩展
- **新方案**: Milvus专业向量数据库
- **优势**: 更好的向量检索性能、更强的扩展性、专业的向量索引

### 系统架构优化
- 完善的JWT认证机制
- 统一的HTTP客户端和错误处理
- 企业级的配置管理和环境隔离
- 完整的监控和日志体系

## 📚 项目文档

完整的项目文档位于 `docs/` 目录：

- [📋 需求分析](docs/01-需求分析/需求分析.md)
- [🏗️ 架构设计](docs/02-架构设计/架构设计.md)
- [📐 详细设计](docs/03-详细设计/详细设计.md)
- [🧪 测试方案](docs/04-测试方案/测试方案.md)
- [🚀 部署与启动](docs/05-部署与启动/部署与启动.md)
- [📊 监控与运维](docs/06-监控与运维/监控与运维.md)

### 专项文档
- [数据库初始化指南](docs/数据库初始化指南.md)
- [配置文件说明](docs/配置文件说明.md)
- [JWT问题解决方案](docs/JWT问题解决方案.md)

## 🔄 主要改进

### 向量数据库迁移
- **原方案**: PostgreSQL + pgvector扩展
- **新方案**: Milvus专业向量数据库
- **优势**: 更好的向量检索性能、更强的扩展性、专业的向量索引

### 系统架构优化
- 完善的JWT认证机制
- 统一的HTTP客户端和错误处理

## 🤝 贡献指南

### 开发流程
1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## ⚠️ 免责声明

- 本项目仅用于学习和技术研究目的
- 不涉及商业化应用
- 请遵守相关API服务的使用条款
- 原项目协议未明确，本项目不声明特定开源协议

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 [GitHub Issues](../../issues)
- 技术交流和学习讨论

---

**最后更新**: 2024-01-07  
**项目状态**: 开发中  
**学习目的**: RAG架构实践、向量数据库应用、Spring AI生态