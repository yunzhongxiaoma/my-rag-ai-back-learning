# Bug与优化跟踪

## 问题列表
| 类型       | 描述                             | 优先级 | 状态  |
|------------|--------------------------------|--------|-----|
| **Bug**    | 重新打开AI问答界面，历史聊天记录丢失(切换功能标签、f5) | 高     | 已完成 |
| **Bug**    | 令牌过期后，未自动跳转到登录界面               | 高     | 已完成 |
| **优化需求** | 多知识库选择+分类型管理                         | 高     | 已完成 |


## 详细代办步骤
### 1. Bug：重新打开AI问答界面，历史聊天记录丢失
- [X] 设计聊天记录数据库表（字段：id、user_id、session_id、content、create_time）
- [X] 后端开发聊天记录的“新增/查询”接口
- [X] 前端在AI问答界面加载时，调用“查询历史记录”接口并渲染


### 2. Bug：令牌过期后，未自动跳转到登录界面
- [X] 后端接口统一拦截Token过期异常，返回特定状态码（如401）
- [X] 前端封装响应拦截器，捕获401状态码
- [X] 配置前端路由守卫，收到401后自动跳转到登录页


## 已完成问题详细解决方案

### 聊天历史记录丢失问题解决方案

**问题描述**：重新打开AI问答界面，历史聊天记录丢失（切换功能标签、F5刷新）

**问题分析**：
- 前端页面刷新后，聊天记录只存储在内存中，导致数据丢失
- 缺少与后端历史记录接口的集成
- 会话管理机制不完善，无法保持聊天连续性

**解决方案**：
1. **前端API扩展**
   - 新增 `getChatHistoryApi()` 接口，调用后端 `/api/v1/chat/history`
   - 新增 `getChatSessionsApi()` 接口，支持会话列表管理
   - 完善接口类型定义：`ChatMessageVO`、`ChatSessionVO`、`ApiResponse`

2. **智能历史记录加载策略**
   - 优先级1：从服务器加载历史记录
   - 优先级2：加载本地缓存数据
   - 优先级3：显示默认欢迎消息

3. **会话管理优化**
   - 实现会话ID持久化存储
   - 消息发送时自动包含会话ID参数
   - 支持会话状态恢复和连续性保持

4. **用户体验改进**
   - 添加历史记录加载状态提示
   - 提供手动刷新历史记录功能
   - 完善错误处理和重试机制
   - 优化UI更新频率，避免过度渲染

**技术实现细节**：
```typescript
// 核心实现：智能历史记录加载
const loadChatHistoryFromServer = async () => {
  try {
    const historyData = await getChatHistoryApi(savedSessionId, 100)
    if (historyData && historyData.length > 0) {
      const historyMessages = historyData.map(convertVOToMessage)
      messages.value = historyMessages
      return true
    }
  } catch (error) {
    console.warn('从服务器加载聊天历史失败:', error)
    return false
  }
}
```

**数据转换逻辑**：
```typescript
// 后端VO转前端Message格式
const convertVOToMessage = (vo: ChatMessageVO): ChatMessage => {
  return {
    role: vo.messageType === 'USER' ? 'user' : 'assistant',
    content: vo.content,
    id: vo.id,
    createTime: vo.createTime
  }
}
```

**测试验证**：
- ✅ 页面刷新后能正确恢复聊天历史
- ✅ 会话连续性保持良好
- ✅ 网络异常时有合理的降级策略
- ✅ 用户体验流畅，加载状态清晰

**影响范围**：
- 前端文件：`my-rag-ai-front-learning/src/api/ChatApi.ts`
- 前端文件：`my-rag-ai-front-learning/src/view/ragChat/RagChatView.vue`
- 前端文件：`my-rag-ai-front-learning/src/utils/fetchWrapper.ts`

**完成时间**：2026-01-09  
**解决者**：yunzhongxiaoma  
**状态**：已完成并测试通过

### Milvus API 过时方法和类型转换问题解决方案

**问题描述**：编译时出现两个错误
1. `VectorStoreManagerImpl.java` 中 `withFieldTypes()` 方法已过时
2. `AliOssFileServiceImpl.java` 中 `Integer` 无法转换为 `Long` 类型

**问题分析**：
- Milvus Java SDK 版本更新，`CreateCollectionParam.Builder.withFieldTypes()` 方法被标记为过时
- `AliOssFile` 实体类的 `id` 字段定义为 `Integer` 类型，但业务方法需要 `Long` 类型参数

**解决方案**：

1. **Milvus API 更新**
   - 将过时的 `withFieldTypes()` 方法替换为新的 `withSchema()` 方法
   - 使用 `CollectionSchemaParam.newBuilder().withFieldTypes(fieldsSchema).build()` 包装字段定义

2. **数据类型统一**
   - 将 `AliOssFile` 实体类的 `id` 字段类型从 `Integer` 改为 `Long`
   - 确保与 MyBatis Plus 和数据库主键类型保持一致

**技术实现细节**：

```java
// 修复前（过时方法）
CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
    .withCollectionName(collectionName)
    .withDescription("知识库 " + knowledgeBaseId + " 的向量集合")
    .withFieldTypes(fieldsSchema)  // 过时方法
    .build();

// 修复后（新方法）
CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
    .withCollectionName(collectionName)
    .withDescription("知识库 " + knowledgeBaseId + " 的向量集合")
    .withSchema(CollectionSchemaParam.newBuilder()
            .withFieldTypes(fieldsSchema)
            .build())
    .build();
```

```java
// 修复前
@TableId
private Integer id;  // 类型不匹配

// 修复后
@TableId
private Long id;     // 统一使用 Long 类型
```

**测试验证**：
- ✅ 项目编译成功，无编译错误
- ✅ Milvus 向量集合创建功能正常
- ✅ OSS 文件删除功能类型匹配正确

**影响范围**：
- 后端文件：`my-rag-ai-back-learning/src/main/java/com/kinghy/rag/service/impl/VectorStoreManagerImpl.java`
- 后端文件：`my-rag-ai-back-learning/src/main/java/com/kinghy/rag/entity/AliOssFile.java`

**完成时间**：2026-01-09  
**解决者**：yunzhongxiaoma  
**状态**：已完成并测试通过