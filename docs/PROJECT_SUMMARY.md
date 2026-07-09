# PMP 项目管理系统 - 项目完成总结

## 项目概述

已完成基于 Spring Boot 的项目管理系统的开发，包含管理端和工作端两个主要模块。

## 技术栈

- **Java 8**
- **Spring Boot 2.7.18** (Tomcat 9.0.107)
- **Spring Security** (认证授权)
- **Spring Data JPA** (数据访问)
- **H2 Database** (本地文件存储)
- **Thymeleaf** (模板引擎)
- **Maven** (构建工具)

## 项目结构

```
pmp/
├── src/main/java/com/pmp/
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── DataInitializer.java
│   ├── controller/          # 控制器
│   │   ├── AdminController.java
│   │   ├── WorkerController.java
│   │   └── AuthController.java
│   ├── dto/                 # 数据传输对象（含 PointsAdjustRequest/Response 新增）
│   │   ├── AssignmentRequest.java
│   │   ├── AssignmentResponse.java
│   │   ├── LoginRequest.java / LoginResponse.java
│   │   ├── PointsAdjustRequest.java / PointsAdjustResponse.java
│   │   ├── PointsConsumeRequest.java
│   │   ├── ProjectRequest.java / ProjectResponse.java
│   │   ├── TaskCompleteRequest.java
│   │   ├── UserRequest.java / UserResponse.java
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Project.java
│   │   ├── ProjectAssignment.java
│   │   ├── TaskExecution.java
│   │   └── PointsTransaction.java
│   ├── enumeration/         # 枚举类
│   │   ├── Role.java
│   │   ├── ProjectType.java
│   │   ├── RepeatType.java
│   │   ├── AssignmentStatus.java
│   │   └── TransactionType.java
│   ├── repository/          # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── ProjectRepository.java
│   │   ├── AssignmentRepository.java
│   │   ├── TaskExecutionRepository.java
│   │   └── PointsTransactionRepository.java
│   ├── service/             # 服务层
│   │   ├── UserService.java
│   │   ├── ProjectService.java
│   │   ├── AssignmentService.java
│   │   └── TaskService.java
│   └── exception/           # 异常处理
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── templates/           # 前端页面
│   │   ├── login.html
│   │   ├── admin/
│   │   │   ├── index.html
│   │   │   ├── projects.html
│   │   │   ├── users.html
│   │   │   ├── assignments.html
│   │   │   └── points.html
│   │   └── worker/
│   │       ├── index.html
│   │       ├── tasks.html
│   │       └── points.html
│   └── application.yml      # 应用配置
├── data/                    # H2 数据库文件存储
└── docs/                    # 项目文档
    ├── FEATURE_GUIDE.md     # 功能说明
    ├── SYSTEM_DESIGN.md     # 系统设计
    ├── DATABASE_DESIGN.md   # 数据库设计
    └── PROJECT_SUMMARY.md   # 本文件
```

## 功能实现

### 1. 认证授权
- 基于 Spring Security 的登录认证
- 管理员和普通用户角色分离
- 默认管理员账户: `admin/admin123`

### 2. 管理端功能 (`/admin`)

#### 项目管理
- 创建项目（支持单价计费和消耗积分两种类型）
- 设置重复方式（每日/每周/不重复）
- 配置单价、消耗积分和每周重复日
- 删除项目

#### 人员管理
- 创建工作端用户账号
- 查看用户列表

#### 项目分配
- 将项目分配给工作人员
- 设置开始和结束日期
- 查看所有分配记录
- 取消分配

#### 积分管理
- 查看所有积分交易记录
- 监控积分使用情况
- **管理员实时调整积分**（选择用户、输入数量、增加/扣除）

### 3. 工作端功能 (`/worker`)

#### 任务管理
- 查看分配的任务列表
- 填写每日任务完成数量
- 自动计算获得积分
- 查看任务完成历史

#### 积分管理
- 查看当前积分余额
- 使用积分消耗功能
- 选择消耗积分的项目

## 数据库设计

### 主要实体
1. **User** — 用户表
2. **Project** — 项目表
3. **ProjectAssignment** — 项目分配表
4. **TaskExecution** — 任务执行记录表
5. **PointsTransaction** — 积分交易记录表（记录用户积分增减明细）

## 业务流程

### 1. 单价计费流程
1. 管理员创建单价计费项目
2. 管理员将项目分配给工作人员
3. 工作人员完成任务，填写完成数量
4. 系统自动计算并发放积分（单价 × 数量）

### 2. 积分消耗流程
1. 管理员创建消耗积分项目
2. 工作人员获得积分
3. 工作人员在积分管理页面选择项目消耗积分
4. 系统扣除相应积分并记录交易

### 3. 管理员手动调整积分（新增）
1. 管理员在积分管理页面选择目标用户
2. 输入调整积分数和原因
3. 选择增加或扣除
4. 系统记录交易，更新余额

## 配置说明

### 数据库配置
- **类型**: H2 数据库（本地文件存储）
- **文件位置**: `./data/pmp`
- **连接池**: 自动配置
- **控制台**: http://localhost:8080/h2-console

### 安全配置
- **登录页面**: `/login`
- **默认管理员**: admin/admin123
- **密码加密**: BCrypt
- **CSRF**: 已禁用（简化开发）

## 运行方式

```bash
mvn clean package
java -jar target/pmp-1.0.0.jar
```

### 访问地址
- **应用首页**: http://localhost:8080
- **管理端**: http://localhost:8080/admin
- **工作端**: http://localhost:8080/worker
- **H2 控制台**: http://localhost:8080/h2-console（JDBC URL: `jdbc:h2:./data/pmp`）

## 使用说明

### 初始化
1. 启动应用，系统自动创建默认管理员账户（admin/admin123）
2. 使用管理员账户登录管理端

### 管理端操作
1. **创建项目**: 在"项目管理"页面创建新项目
2. **创建用户**: 在"人员管理"页面创建工作人员账号
3. **分配任务**: 在"任务分配"页面将项目分配给工作人员
4. **调整积分**: 在"积分管理"页面手动调整用户积分

### 工作端操作
1. **完成任务**: 在"任务管理"页面填写今日任务完成数量
2. **消耗积分**: 在"积分管理"页面使用积分

## 完成状态

✅ 所有核心功能已实现
✅ 管理端和工作端完整可用
✅ 数据库设计合理
✅ 安全认证完善
✅ 用户界面友好
✅ 管理员实时调整积分功能
✅ 无编译依赖外部仓库问题（Tomcat 9.0.107 覆写）
✅ 三类文档齐全（功能说明/系统设计/数据库设计）

项目已完成，可以正常运行使用。