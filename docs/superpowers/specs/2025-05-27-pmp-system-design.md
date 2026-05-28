# PMP 项目管理系统设计文档

## 1. 系统概述

**项目名称**：PMP 项目管理系统  
**创建日期**：2025-05-27  
**技术栈**：SpringBoot 2.7.x + JDK 8 + H2 数据库 + 纯 HTML/CSS/JavaScript

**系统目标**：
- 管理端：分配项目、管理用户、查看统计
- 工作端：填写每日任务、完成数量、执行积分消耗项目、查看个人统计
- 支持单价计费和积分消耗两种项目类型
- 支持每日、每周任务自动分配

## 2. 系统架构

### 2.1 架构模式
采用单体分层架构（Controller → Service → Repository）

### 2.2 技术选型
- **后端框架**：SpringBoot 2.7.x
- **开发语言**：Java 8
- **数据库**：H2 嵌入式数据库（文件模式）
- **ORM框架**：Spring Data JPA
- **安全框架**：Spring Security
- **前端技术**：纯 HTML/CSS/JavaScript + Bootstrap
- **构建工具**：Maven

### 2.3 分层结构
- **Controller 层**：处理 HTTP 请求，参数验证，调用 Service
- **Service 层**：业务逻辑处理，事务管理
- **Repository 层**：数据库操作
- **Entity 层**：数据模型，对应数据库表

## 3. 数据模型设计

### 3.1 User（用户）
```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;        // 用户名（唯一）
    private String password;        // 密码（BCrypt加密）
    @Enumerated(EnumType.STRING)
    private Role role;              // 角色：ADMIN, WORKER
    private LocalDateTime createdAt;
}
```

### 3.2 Project（项目）
```java
@Entity
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;            // 项目名称
    @Enumerated(EnumType.STRING)
    private ProjectType type;       // 类型：RATE_BASED, POINT_CONSUMING
    private BigDecimal unitPrice;   // 单价（RATE_BASED类型使用）
    @Enumerated(EnumType.STRING)
    private RepeatType repeatType;  // 重复方式：NONE, DAILY, WEEKLY
    private Integer repeatDay;      // 重复星期（WEEKLY类型使用，1-7）
    private Integer pointsToConsume; // 消耗积分数（POINT_CONSUMING类型使用）
    private Long createdBy;         // 创建者（管理员）
    private LocalDateTime createdAt;
}
```

### 3.3 ProjectAssignment（项目分配）
```java
@Entity
public class ProjectAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Project project;
    @ManyToOne
    private User worker;
    private LocalDate assignDate;   // 分配日期
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status; // 状态：ACTIVE, COMPLETED
    private Integer completedQuantity; // 完成数量
}
```

### 3.4 TaskExecution（任务执行）
```java
@Entity
public class TaskExecution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private ProjectAssignment assignment;
    private LocalDate executionDate; // 执行日期
    private Integer quantity;        // 完成数量
    private Integer pointsEarned;    // 获得积分数
    private Integer pointsConsumed;  // 消耗积分数
    private LocalDateTime createdAt;
}
```

### 3.5 PointsTransaction（积分交易）
```java
@Entity
public class PointsTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @Enumerated(EnumType.STRING)
    private TransactionType type;    // 交易类型：EARN, CONSUME
    private Integer amount;          // 交易金额
    private String description;      // 交易描述
    private LocalDateTime createdAt;
}
```

## 4. 功能模块设计

### 4.1 认证模块
- 管理员和工作端用户登录/登出
- Session 管理
- 权限验证（基于角色的访问控制）

### 4.2 项目管理端模块

#### 项目管理
- 创建项目（选择重复方式、单价、消耗积分数等）
- 查看项目列表
- 编辑/删除项目

#### 项目分配
- 为工作端用户分配项目
- 查看分配记录
- 根据重复方式自动创建周期性任务

#### 人员管理
- 创建工作端用户账号
- 查看用户列表
- 禁用/启用用户

#### 统计报表
- 查看所有用户的任务完成情况
- 查看积分获得和消耗统计

### 4.3 工作端模块

#### 操作页面
- 查看今日分配的任务
- 填写任务完成数量
- 执行积分消耗项目

#### 统计页面
- 查看个人任务完成记录
- 查看个人积分余额
- 查看积分交易明细

## 5. 页面设计

### 5.1 登录页面
- 用户名/密码输入框
- 登录按钮
- 根据角色自动跳转到相应端（管理端/工作端）

### 5.2 管理端页面
- **导航栏**：项目管理、项目分配、人员管理、统计报表、退出
- **项目管理页面**：项目列表表格、新建项目按钮、编辑/删除操作
- **项目分配页面**：选择用户、选择项目、设置重复方式、确认分配
- **人员管理页面**：用户列表、新建用户按钮、禁用/启用操作
- **统计报表页面**：按用户统计完成任务数、积分获得/消耗统计

### 5.3 工作端页面
- **导航栏**：我的任务、积分消耗、统计报表、退出
- **我的任务页面**：显示今日分配的任务、填写完成数量的输入框、提交按钮
- **积分消耗页面**：显示可消耗积分的项目列表、执行按钮、输入消耗数量
- **统计页面**：个人任务记录列表、积分余额、交易明细

## 6. 技术实现细节

### 6.1 SpringBoot 配置
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/pmp;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 6.2 认证安全
- 密码使用 BCrypt 加密
- 基于 Session 的认证
- 角色权限注解控制（@PreAuthorize）

### 6.3 前端交互
- 使用 fetch API 发送 AJAX 请求
- JSON 数据格式
- Bootstrap 用于响应式布局
- 简单的 JavaScript 处理表单提交和页面刷新

### 6.4 定时任务
- 使用 Spring @Scheduled 自动创建每日/每周任务
- 每日凌晨检查需要创建的周期性任务

## 7. 错误处理和边界情况

### 7.1 错误处理
- 全局异常处理器（@ControllerAdvice）
- 友好的错误提示信息
- 数据验证失败的处理
- 权限不足的提示

### 7.2 边界情况处理
- **积分不足**：执行积分消耗项目时检查余额，不足时提示
- **重复分配**：同一用户同一项目不重复分配
- **已完成任务**：已完成的任务不再显示在待办列表
- **用户不存在**：分配时检查用户有效性
- **项目不存在**：分配时检查项目有效性
- **数据库初始化**：首次启动时创建默认管理员账号
- **并发控制**：防止同一任务被多次提交

### 7.3 数据验证
- 前端和后端双重验证
- 完成数量不能为负数
- 积分消耗数量不能超过余额
- 必填字段检查

## 8. 枚举定义

```java
// 用户角色
public enum Role {
    ADMIN, WORKER
}

// 项目类型
public enum ProjectType {
    RATE_BASED,       // 单价计费
    POINT_CONSUMING   // 积分消耗
}

// 重复方式
public enum RepeatType {
    NONE,    // 不重复
    DAILY,   // 每日
    WEEKLY   // 每周
}

// 分配状态
public enum AssignmentStatus {
    ACTIVE,     // 进行中
    COMPLETED   // 已完成
}

// 交易类型
public enum TransactionType {
    EARN,     // 获得
    CONSUME   // 消耗
}
```

## 9. 初始化数据

系统首次启动时创建默认管理员账号：
- 用户名：admin
- 密码：admin123
- 角色：ADMIN

## 10. 项目结构

```
pmp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── pmp/
│   │   │           ├── PmpApplication.java
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── AdminController.java
│   │   │           │   └── WorkerController.java
│   │   │           ├── service/
│   │   │           │   ├── UserService.java
│   │   │           │   ├── ProjectService.java
│   │   │           │   ├── AssignmentService.java
│   │   │           │   └── PointsService.java
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── ProjectRepository.java
│   │   │           │   ├── AssignmentRepository.java
│   │   │           │   └── PointsTransactionRepository.java
│   │   │           ├── entity/
│   │   │           │   ├── User.java
│   │   │           │   ├── Project.java
│   │   │           │   ├── ProjectAssignment.java
│   │   │           │   ├── TaskExecution.java
│   │   │           │   └── PointsTransaction.java
│   │   │           ├── dto/
│   │   │           │   ├── LoginRequest.java
│   │   │           │   ├── ProjectRequest.java
│   │   │           │   └── AssignmentRequest.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   └── WebConfig.java
│   │   │           └── exception/
│   │   │               ├── GlobalExceptionHandler.java
│   │   │               └── BusinessException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── img/
│   │       └── templates/
│   │           ├── login.html
│   │           ├── admin/
│   │           │   ├── dashboard.html
│   │           │   ├── projects.html
│   │           │   ├── assignments.html
│   │           │   ├── users.html
│   │           │   └── statistics.html
│   │           └── worker/
│   │               ├── dashboard.html
│   │               ├── tasks.html
│   │               ├── points.html
│   │               └── statistics.html
├── data/                          # H2数据库文件存储目录
└── docs/                          # 文档目录
```

## 11. 业务流程

### 11.1 项目分配流程
1. 管理员创建项目（设置类型、单价、重复方式等）
2. 管理员选择工作端用户并分配项目
3. 系统根据重复方式自动创建周期性任务
4. 工作端用户登录查看分配的任务

### 11.2 积分获得流程
1. 工作端用户完成每日任务
2. 填写完成数量并提交
3. 系统计算积分数（完成数量 × 单价）
4. 积分记录到 PointsTransaction
5. 更新用户积分余额

### 11.3 积分消耗流程
1. 工作端用户查看可消耗积分的项目列表
2. 选择项目并输入消耗数量
3. 系统检查积分余额是否充足
4. 如充足，扣除积分并记录交易
5. 如不足，提示用户

## 12. API 接口设计

### 12.1 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

### 12.2 管理端接口
- `GET /api/admin/projects` - 获取项目列表
- `POST /api/admin/projects` - 创建项目
- `PUT /api/admin/projects/{id}` - 更新项目
- `DELETE /api/admin/projects/{id}` - 删除项目
- `POST /api/admin/assignments` - 分配项目
- `GET /api/admin/assignments` - 获取分配列表
- `GET /api/admin/users` - 获取用户列表
- `POST /api/admin/users` - 创建用户
- `PUT /api/admin/users/{id}` - 更新用户
- `GET /api/admin/statistics` - 获取统计数据

### 12.3 工作端接口
- `GET /api/worker/tasks` - 获取今日任务
- `POST /api/worker/tasks/{id}/complete` - 完成任务
- `GET /api/worker/point-projects` - 获取可消耗积分的项目
- `POST /api/worker/points/consume` - 消耗积分
- `GET /api/worker/statistics` - 获取个人统计
- `GET /api/worker/points/balance` - 获取积分余额

## 13. 安全考虑

- 密码加密存储（BCrypt）
- 基于角色的访问控制（RBAC）
- Session 过期时间设置
- 防止 CSRF 攻击
- SQL 注入防护（使用 JPA）
- XSS 防护（前端输入验证）

## 14. 性能优化

- 数据库连接池配置
- 使用索引优化查询
- 分页查询大数据量
- 前端资源压缩和缓存
- 减少不必要的数据库查询

## 15. 部署说明

- 打包为可执行 JAR 文件
- 数据文件存储在 `./data` 目录
- 支持命令行启动：`java -jar pmp.jar`
- 支持 Windows 服务部署
- 默认端口：8080
- 默认管理员账号：admin/admin123

---

**设计文档版本**：1.0  
**最后更新**：2025-05-27  
**设计者**：UMS Code