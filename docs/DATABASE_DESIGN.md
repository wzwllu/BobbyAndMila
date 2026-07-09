# PMP 项目管理系统 — 数据库设计文档

**文档版本**：2.0
**最后更新**：2026-07-08
**数据库**：H2 Database（嵌入式文件模式）
**存放路径**：`./data/pmp`

---

## 1. 实体关系总图

```
┌──────────┐     ┌──────────────┐     ┌──────────────────┐
│   User   │────→│ProjectAssign │←────│     Project      │
│          │     │   ment       │     │                  │
│ - id     │     │              │     │ - id             │
│ - uname │     │ - id         │     │ - name           │
│ - pwd   │     │ - project ───│──→  │ - type           │
│ - role  │     │ - user  ─────│─→   │ - unitPrice      │
└────┬─────┘     │ - startDate  │     │ - repeatType     │
     │          │ - endDate    │     │ - repeatDay      │
     │          │ - status     │     │ - ptsToConsume   │
     │          │ - createdBy  │     │ - createdBy      │
     │          │ - createdAt   │     │ - createdAt      │
     │          └──────┬───────┘     └──────────────────┘
     │                 │
     │          ┌──────┴───────┐
     │          │TaskExecution │
     │          │              │
     │          │ - id         │
     │          │ - assignment→│  (FK→ProjectAssignment)
     │          │ - execDate   │
     │          │ - quantity   │
     │          │ - ptsEarned  │
     │          │ - ptsConsumed│
     │          │ - createdAt  │
     │          └──────────────┘
     │
     │    ┌─────────────────────┐
     └───→│  PointsTransaction  │
          │                     │
          │ - id                │
          │ - user──────────────│──→ FK→User
          │ - type (EARN/CONSUME│
          │ - amount            │
          │ - description       │
          │ - assignment (opt)──│──→ FK→ProjectAssignment
          │ - taskExec (opt)────│──→ FK→TaskExecution
          │ - createdAt         │
          └─────────────────────┘
```

---

## 2. 表结构

### 2.1 users — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(255) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| role | VARCHAR(20) | NOT NULL | `ADMIN` / `WORKER` |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**DDL**：
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

**初始化数据**：
```sql
INSERT INTO users (username, password, role, created_at)
VALUES ('admin', '$2a$10$...', 'ADMIN', NOW());
```

---

### 2.2 projects — 项目表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 项目ID |
| name | VARCHAR(255) | NOT NULL | 项目名称 |
| type | VARCHAR(20) | NOT NULL | `RATE_BASED` / `POINT_CONSUMING` |
| unit_price | DECIMAL(10,2) | NULLABLE | 单价（RATE_BASED 类型使用）|
| repeat_type | VARCHAR(20) | NOT NULL | `NONE` / `DAILY` / `WEEKLY` |
| repeat_day | INT | NULLABLE | 每周重复日 1-7（WEEKLY 类型使用）|
| points_to_consume | INT | NULLABLE | 消耗积分数（POINT_CONSUMING 类型使用）|
| created_by | BIGINT | NOT NULL | 创建者ID |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**DDL**：
```sql
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10,2),
    repeat_type VARCHAR(20) NOT NULL,
    repeat_day INT,
    points_to_consume INT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

---

### 2.3 project_assignments — 项目分配表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 分配ID |
| project_id | BIGINT | FK→projects.id, NOT NULL | 关联项目 |
| user_id | BIGINT | FK→users.id, NOT NULL | 关联工作人员 |
| start_date | DATE | NULLABLE | 开始日期 |
| end_date | DATE | NULLABLE | 结束日期 |
| status | VARCHAR(20) | NOT NULL | `ACTIVE` / `COMPLETED` / `CANCELLED` |
| created_by | BIGINT | NULLABLE | 分配者（管理员ID）|
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**DDL**：
```sql
CREATE TABLE project_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

### 2.4 task_executions — 任务执行记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 记录ID |
| assignment_id | BIGINT | FK, NOT NULL | 关联分配 |
| execution_date | DATE | NOT NULL | 执行日期 |
| quantity | INT | NULLABLE | 完成数量 |
| points_earned | INT | NULLABLE | 获得积分数 |
| points_consumed | INT | NULLABLE | 消耗积分数 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**DDL**：
```sql
CREATE TABLE task_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    execution_date DATE NOT NULL,
    quantity INT,
    points_earned INT,
    points_consumed INT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (assignment_id) REFERENCES project_assignments(id)
);
```

---

### 2.5 points_transactions — 积分交易表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 交易ID |
| user_id | BIGINT | FK→users.id, NOT NULL | 关联用户 |
| assignment_id | BIGINT | FK, NULLABLE | 关联分配（可选）|
| task_execution_id | BIGINT | FK, NULLABLE | 关联任务执行（可选）|
| type | VARCHAR(20) | NOT NULL | `EARN`(获得) / `CONSUME`(消耗) |
| amount | INT | NOT NULL | 交易积分数 |
| description | VARCHAR(255) | NULLABLE | 交易描述 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**DDL**：
```sql
CREATE TABLE points_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    assignment_id BIGINT,
    task_execution_id BIGINT,
    type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (assignment_id) REFERENCES project_assignments(id),
    FOREIGN KEY (task_execution_id) REFERENCES task_executions(id)
);
```

---

## 3. 关联关系

| 源表 | 目标表 | 外键字段 | 关系 |
|------|--------|---------|------|
| project_assignments | projects | project_id | N:1 |
| project_assignments | users | user_id | N:1 |
| task_executions | project_assignments | assignment_id | N:1 |
| points_transactions | users | user_id | N:1 |
| points_transactions | project_assignments | assignment_id | N:1 (nullable) |
| points_transactions | task_executions | task_execution_id | N:1 (nullable) |

---

## 4. 核心查询

### 4.1 计算用户积分余额

```java
@Query("SELECT COALESCE(SUM(CASE WHEN pt.type = 'EARN' THEN pt.amount ELSE -pt.amount END), 0) " +
       "FROM PointsTransaction pt WHERE pt.user.id = :userId")
Integer sumPointsByUserId(@Param("userId") Long userId);
```

等价 SQL：
```sql
SELECT COALESCE(SUM(
    CASE WHEN type = 'EARN' THEN amount ELSE -amount END
), 0) AS balance
FROM points_transactions
WHERE user_id = ?
```

### 4.2 获得/消耗分类统计

```java
// 获得积分合计
@Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt " +
       "WHERE pt.user = :user AND pt.type = 'EARN'")

// 消耗积分合计
@Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt " +
       "WHERE pt.user = :user AND pt.type = 'CONSUME'")
```

### 4.3 用户任务执行历史

```java
@Query("SELECT te FROM TaskExecution te " +
       "WHERE te.assignment.user.id = :userId " +
       "ORDER BY te.executionDate DESC")
```

---

## 5. 实体字段详解

### 5.1 TaskExecution

| 字段 | 赋值场景 | 值来源 |
|------|---------|--------|
| `pointsEarned` | 完成任务时 | `unitPrice × quantity` |
| `pointsConsumed` | 未使用 | — |

> `pointsEarned` 在 **`TaskService.completeTask()`** 中被赋值，用于记录该次任务产生的积分。

### 5.2 PointsTransaction

| 字段 | 完成任务的交易 | 消耗积分的交易 | 管理员调整 |
|------|--------------|---------------|----------|
| `user` | 分配中的 worker | 当前用户 | 管理员选定的用户 |
| `assignment` | 关联的分配 | 关联的分配 | null |
| `taskExecution` | 关联的执行记录 | null | null |
| `type` | EARN | CONSUME | EARN/CONSUME |
| `amount` | `unitPrice × quantity` | `points` | `Math.abs(amount)` |
| `description` | "完成任务: {项目名}" | "消耗积分: {项目名}" | "管理员手动调整" / 自定义 |

---

## 6. 枚举与数据库存储

所有枚举使用 `@Enumerated(EnumType.STRING)` 存储字符串值。

| 枚举 | Java 定义 | DB 存储 |
|------|----------|---------|
| Role | ADMIN, WORKER | `'ADMIN'`, `'WORKER'` |
| ProjectType | RATE_BASED, POINT_CONSUMING | `'RATE_BASED'`, `'POINT_CONSUMING'` |
| RepeatType | NONE, DAILY, WEEKLY | `'NONE'`, `'DAILY'`, `'WEEKLY'` |
| AssignmentStatus | ACTIVE, COMPLETED, CANCELLED | `'ACTIVE'`, `'COMPLETED'`, `'CANCELLED'` |
| TransactionType | EARN, CONSUME | `'EARN'`, `'CONSUME'` |

---

## 7. 表与实体映射

| 数据库表 | JPA Entity | Repository |
|----------|-----------|------------|
| `users` | `User.java` | `UserRepository` |
| `projects` | `Project.java` | `ProjectRepository` |
| `project_assignments` | `ProjectAssignment.java` | `AssignmentRepository` |
| `task_executions` | `TaskExecution.java` | `TaskExecutionRepository` |
| `points_transactions` | `PointsTransaction.java` | `PointsTransactionRepository` |

---

## 8. 字段变更记录（v1 → v2）

| 表 | 字段 | v1 | v2 | 说明 |
|----|------|----|----|------|
| points_transactions | amount | ~~`points`~~ (旧) | `amount` | 统一命名，避免混淆 |

<!-- 文档结束 -->