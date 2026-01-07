# RAG AI 后端学习项目

## 文档概述

本文档集合为 `my-rag-ai-back-learning` 项目提供完整的企业级规范文档，涵盖从需求分析到运维监控的全生命周期管理。

## 项目背景

本项目是基于 [kinghy-rag-ai-back](https://github.com/kinghy949/kinghy-rag-ai-back) 的复刻学习实践项目，主要目标是：
- 将原有的PostgreSQL向量存储方案迁移至Milvus向量数据库
- 构建基于Spring AI的RAG（检索增强生成）智能问答系统
- 学习和实践企业级RAG系统的完整架构

## 技术栈

- **后端框架**: Spring Boot 3.4.2, Spring AI 1.0.0
- **开发语言**: Java 17
- **数据存储**: MySQL 8.0+, Milvus 2.5+, Redis 6.0+
- **对象存储**: 阿里云OSS
- **AI服务**: 阿里云通义千问
- **构建工具**: Maven 3.9.9
- **容器化**: Docker, Docker Compose

## 文档结构

### 📋 [01-需求分析](./01-需求分析/需求分析.md)
- 项目背景和业务目标
- 用户画像和使用场景
- 功能需求（核心功能/扩展功能）
- 非功能需求（性能/安全/兼容性）
- 约束条件和风险评估

### 🏗️ [02-架构设计](./02-架构设计/架构设计.md)
- 系统整体架构设计
- 技术栈选型和理由说明
- 核心模块设计和职责划分
- 数据流向和依赖关系
- 安全架构和性能优化设计

### 📐 [03-详细设计](./03-详细设计/详细设计.md)
- RESTful API接口设计
- 数据库表结构设计
- 核心算法和业务逻辑设计
- 前后端交互时序图
- 缓存策略和异常处理设计

### 🧪 [04-测试方案](./04-测试方案/测试方案.md)
- 测试策略和测试类型
- 单元测试/集成测试/接口测试
- 性能测试和安全测试
- 测试用例设计和执行
- 缺陷管理和质量度量

### 🚀 [05-部署与启动](./05-部署与启动/部署与启动.md)
- 环境依赖和配置要求
- 本地开发环境搭建
- 测试环境和生产环境部署
- 配置文件说明和参数调优
- 初始化流程和启动命令

### 📊 [06-监控与运维](./06-监控与运维/监控与运维.md)
- 监控指标体系和告警规则
- 日志采集规范和分析
- 故障排查流程和工具
- 版本回滚和应急处理
- 性能优化和容量规划

## 快速开始

### 环境准备
1. 安装 JDK 17+
2. 安装 Maven 3.9+
3. 安装 Docker 和 Docker Compose
4. 准备阿里云通义千问 API Key
5. 准备阿里云OSS配置

### 本地启动
```bash
# 1. 克隆项目
git clone <your-repo-url>
cd my-rag-ai-back-learning

# 2. 启动数据库服务
docker-compose -f docs/docker-milvus-attu-compose.yml up -d

# 3. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入相关配置

# 4. 编译和启动应用
mvn clean package -DskipTests
java -jar target/my-rag-ai-back-learning-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 验证部署
```bash
# 检查应用健康状态
curl http://localhost:8989/actuator/health

# 访问Attu管理界面（可选）
open http://localhost:3000
```

## 文档维护

### 文档更新原则
- 代码变更时同步更新相关文档
- 重要架构调整需要更新架构设计文档
- 新增功能需要更新需求分析和详细设计
- 部署流程变更需要更新部署文档

### 文档版本管理
- 文档版本与代码版本保持同步
- 重大变更需要在文档中标注版本号和变更日期
- 保留历史版本文档以供参考

## 贡献指南

### 文档贡献流程
1. Fork 项目仓库
2. 创建文档分支 `git checkout -b docs/update-xxx`
3. 更新相关文档
4. 提交变更 `git commit -m "docs: 更新xxx文档"`
5. 推送分支 `git push origin docs/update-xxx`
6. 创建 Pull Request

### 文档规范
- 使用 Markdown 格式编写
- 遵循统一的文档结构和格式
- 包含必要的代码示例和配置说明
- 添加适当的图表和流程图（文字描述）
- 保持文档的准确性和时效性

## 联系方式

如有问题或建议，请通过以下方式联系：
- 邮箱: 1043615068@qq.com