# Swagger接口文档使用说明

## 访问地址

重启应用后，可以通过以下地址访问API文档：

### 主要访问地址
- **Knife4j UI（推荐）**: http://localhost:8989/doc.html
- **Swagger UI**: http://localhost:8989/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8989/v3/api-docs

## 使用步骤

### 1. 启动应用
确保Spring Boot应用已经启动，端口为8989。

### 2. 访问文档界面
在浏览器中打开 http://localhost:8989/doc.html

### 3. 获取JWT Token
在使用需要认证的接口之前，需要先获取JWT token：

#### 步骤详解：
1. **找到登录接口**
   - 在左侧接口列表中找到 `UserController` 分组
   - 展开后找到 `POST /api/v1/user/login` 接口

2. **填写登录信息**
   - 点击接口后，点击右侧的 **"试一试"** 按钮
   - 在参数区域填入：
     - `userName`: `admin` （默认管理员账号）
     - `password`: `123456` （默认密码）

3. **执行登录**
   - 点击 **"执行"** 按钮
   - 等待响应返回

4. **复制Token**
   - 登录成功后，响应格式如下：
   ```json
   {
     "code": 0,
     "message": "操作成功", 
     "data": {
       "id": 1,
       "userName": "admin",
       "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImV4cCI6MTY0..."
     }
   }
   ```
   - **复制 `data.token` 的完整值**

#### 如果没有账号，先注册：
1. 使用 `POST /api/v1/user/register` 接口
2. 填入完整的用户信息：
```json
{
  "name": "测试用户",
  "userName": "testuser", 
  "password": "123456",
  "phone": "13800138000",
  "sex": "男",
  "idNumber": "110101199001011234",
  "status": 1
}
```

### 4. 设置全局认证
1. 点击页面右上角的 **"认证"** 按钮（🔒锁图标）
2. 在弹出的对话框中：
   - 选择 **Bearer** 认证方式
   - 在输入框中输入：`Bearer <your-token>`
   - **注意**：`Bearer` 和 token 之间必须有一个空格
   - 例如：`Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImV4cCI6MTY0...`
3. 点击 **"认证"** 按钮
4. 认证成功后，右上角锁图标会变成已认证状态

### 5. 测试接口
现在可以测试所有需要认证的接口了。

## 接口分组

文档按控制器分组：

- **UserController**: 用户管理相关接口
- **KnowledgeController**: 知识库管理接口
- **ChatController**: AI对话接口
- **DrawImageController**: AI绘画接口
- **SensitiveWordController**: 敏感词管理
- **LogInfoController**: 日志管理
- **TestController**: 测试接口

## 常见问题

### 1. 页面显示"暂无文档数据"
**解决方案**：
- 检查应用是否正常启动
- 确认端口8989是否被占用
- 重启应用后再次访问

### 2. 接口返回401错误
**可能原因和解决方案**：
- **用户名密码错误**: 检查输入的用户名和密码是否正确
- **用户不存在**: 先使用注册接口创建账号
- **Token格式错误**: 确保格式为 `Bearer <token>`，注意空格
- **Token过期**: JWT token有效期为2小时，过期后需重新登录
- **未设置认证**: 确认已在右上角设置了Bearer认证

### 3. 无法访问Swagger页面
**解决方案**：
- 检查应用是否在8989端口正常启动
- 确认防火墙没有阻止8989端口
- 查看应用启动日志是否有错误
- 尝试访问 http://localhost:8989/v3/api-docs 检查API文档是否生成

### 4. 默认账号信息
如果不确定系统中的账号，可以尝试：
- **管理员账号**: `admin` / `123456`
- **测试账号**: `test` / `123456`
- 或者使用注册接口创建新账号

## 配置说明

### application.yml配置
```yaml
# Knife4j配置
knife4j:
  enable: true
  setting:
    language: zh_cn
  production: false

# SpringDoc配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
    enabled: true
```

### 安全配置
JWT拦截器已配置排除Swagger相关路径：
- `/doc.html`
- `/webjars/**`
- `/swagger-resources/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

## 开发建议

1. **接口测试**: 使用Swagger UI进行接口测试，比Postman更方便
2. **文档维护**: 及时更新控制器中的`@Operation`注解描述
3. **参数说明**: 为复杂的请求参数添加详细的注解说明
4. **错误码**: 在接口文档中说明可能的错误码和含义

## 技术栈

- **Knife4j**: 4.5.0 (Swagger增强UI)
- **SpringDoc**: Spring Boot 3.x 兼容版本
- **OpenAPI**: 3.0 规范
- **JWT认证**: Bearer Token方式

---

**最后更新时间**: 2026年1月8日  
**文档版本**: v1.0.0