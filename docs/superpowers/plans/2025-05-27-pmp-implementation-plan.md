# PMP 项目管理系统实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 基于SpringBoot 2.7.x + JDK 8 + H2数据库 + 纯HTML/CSS/JavaScript的项目管理系统，支持项目管理、任务分配、积分管理功能。

**架构:** 单体分层架构（Controller → Service → Repository），使用Spring Security进行认证和授权，H2嵌入式数据库存储数据，纯HTML/CSS/JavaScript前端通过AJAX与后端交互。

**技术栈:** SpringBoot 2.7.x、Java 8、H2数据库、Spring Data JPA、Spring Security、Bootstrap、JavaScript

---

## Task 1: 项目初始化和基础配置

**文件:**
- 创建: `pom.xml`
- 创建: `src/main/resources/application.yml`
- 创建: `src/main/java/com/pmp/PmpApplication.java`

- [ ] **Step 1: 创建 Maven 项目配置文件 `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>
    
    <groupId>com.pmp</groupId>
    <artifactId>pmp</artifactId>
    <version>1.0.0</version>
    <name>PMP Project Management System</name>
    
    <properties>
        <java.version>1.8</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建应用配置文件 `src/main/resources/application.yml`**

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
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8080

logging:
  level:
    com.pmp: DEBUG
```

- [ ] **Step 3: 创建 SpringBoot 主类 `src/main/java/com/pmp/PmpApplication.java`**

```java
package com.pmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PmpApplication {
    public static void main(String[] args) {
        SpringApplication.run(PmpApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建数据目录并测试启动**

运行: `mkdir -p data`
运行: `mvn spring-boot:run`
预期: 应用启动成功，在 http://localhost:8080 可以访问

- [ ] **Step 5: 初始化 Git 仓库并提交**

运行: `git init`
运行: `git add pom.xml src/main/resources/application.yml src/main/java/com/pmp/PmpApplication.java`
运行: `git commit -m "feat: initialize SpringBoot project with basic configuration"`

---

## Task 2: 创建枚举类型和实体类

**文件:**
- 创建: `src/main/java/com/pmp/enumeration/Role.java`
- 创建: `src/main/java/com/pmp/enumeration/ProjectType.java`
- 创建: `src/main/java/com/pmp/enumeration/RepeatType.java`
- 创建: `src/main/java/com/pmp/enumeration/AssignmentStatus.java`
- 创建: `src/main/java/com/pmp/enumeration/TransactionType.java`
- 创建: `src/main/java/com/pmp/entity/User.java`
- 创建: `src/main/java/com/pmp/entity/Project.java`
- 创建: `src/main/java/com/pmp/entity/ProjectAssignment.java`
- 创建: `src/main/java/com/pmp/entity/TaskExecution.java`
- 创建: `src/main/java/com/pmp/entity/PointsTransaction.java`

- [ ] **Step 1: 创建 Role 枚举**

```java
package com.pmp.enumeration;

public enum Role {
    ADMIN, WORKER
}
```

- [ ] **Step 2: 创建 ProjectType 枚举**

```java
package com.pmp.enumeration;

public enum ProjectType {
    RATE_BASED, POINT_CONSUMING
}
```

- [ ] **Step 3: 创建 RepeatType 枚举**

```java
package com.pmp.enumeration;

public enum RepeatType {
    NONE, DAILY, WEEKLY
}
```

- [ ] **Step 4: 创建 AssignmentStatus 枚举**

```java
package com.pmp.enumeration;

public enum AssignmentStatus {
    ACTIVE, COMPLETED
}
```

- [ ] **Step 5: 创建 TransactionType 枚举**

```java
package com.pmp.enumeration;

public enum TransactionType {
    EARN, CONSUME
}
```

- [ ] **Step 6: 创建 User 实体**

```java
package com.pmp.entity;

import com.pmp.enumeration.Role;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 7: 创建 Project 实体**

```java
package com.pmp.entity;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;
    
    @Column(scale = 2)
    private BigDecimal unitPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatType repeatType;
    
    private Integer repeatDay;
    
    private Integer pointsToConsume;
    
    @Column(nullable = false)
    private Long createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 8: 创建 ProjectAssignment 实体**

```java
package com.pmp.entity;

import com.pmp.enumeration.AssignmentStatus;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_assignments")
@Data
public class ProjectAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;
    
    @Column(nullable = false)
    private LocalDate assignDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;
    
    private Integer completedQuantity;
    
    @PrePersist
    protected void onCreate() {
        status = AssignmentStatus.ACTIVE;
        completedQuantity = 0;
    }
}
```

- [ ] **Step 9: 创建 TaskExecution 实体**

```java
package com.pmp.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_executions")
@Data
public class TaskExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ProjectAssignment assignment;
    
    @Column(nullable = false)
    private LocalDate executionDate;
    
    @Column(nullable = false)
    private Integer quantity;
    
    private Integer pointsEarned;
    
    private Integer pointsConsumed;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 10: 创建 PointsTransaction 实体**

```java
package com.pmp.entity;

import com.pmp.enumeration.TransactionType;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@Data
public class PointsTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(nullable = false)
    private Integer amount;
    
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 11: 提交实体类代码**

运行: `git add src/main/java/com/pmp/enumeration/ src/main/java/com/pmp/entity/`
运行: `git commit -m "feat: add enumeration types and entity classes"`

---

## Task 3: 创建 Repository 接口

**文件:**
- 创建: `src/main/java/com/pmp/repository/UserRepository.java`
- 创建: `src/main/java/com/pmp/repository/ProjectRepository.java`
- 创建: `src/main/java/com/pmp/repository/AssignmentRepository.java`
- 创建: `src/main/java/com/pmp/repository/TaskExecutionRepository.java`
- 创建: `src/main/java/com/pmp/repository/PointsTransactionRepository.java`

- [ ] **Step 1: 创建 UserRepository**

```java
package com.pmp.repository;

import com.pmp.entity.User;
import com.pmp.enumeration.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    java.util.List<User> findByRole(Role role);
}
```

- [ ] **Step 2: 创建 ProjectRepository**

```java
package com.pmp.repository;

import com.pmp.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedBy(Long createdBy);
}
```

- [ ] **Step 3: 创建 AssignmentRepository**

```java
package com.pmp.repository;

import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.User;
import com.pmp.enumeration.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByWorkerAndAssignDate(User worker, LocalDate assignDate);
    
    Optional<ProjectAssignment> findByWorkerAndProjectAndAssignDate(
        User worker, Project project, LocalDate assignDate
    );
    
    @Query("SELECT a FROM ProjectAssignment a WHERE a.assignDate = :date AND a.status = 'ACTIVE'")
    List<ProjectAssignment> findActiveAssignmentsByDate(@Param("date") LocalDate date);
    
    List<ProjectAssignment> findByWorker(User worker);
}
```

- [ ] **Step 4: 创建 TaskExecutionRepository**

```java
package com.pmp.repository;

import com.pmp.entity.TaskExecution;
import com.pmp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
    List<TaskExecution> findByExecutionDateOrderByCreatedAtDesc(LocalDate date);
    
    List<TaskExecution> findByAssignmentWorkerOrderByExecutionDateDesc(User worker);
    
    @Query("SELECT COALESCE(SUM(te.pointsEarned), 0) FROM TaskExecution te WHERE te.assignment.worker = :worker")
    Integer sumPointsEarnedByWorker(@Param("worker") User worker);
    
    @Query("SELECT COALESCE(SUM(te.pointsConsumed), 0) FROM TaskExecution te WHERE te.assignment.worker = :worker")
    Integer sumPointsConsumedByWorker(@Param("worker") User worker);
}
```

- [ ] **Step 5: 创建 PointsTransactionRepository**

```java
package com.pmp.repository;

import com.pmp.entity.PointsTransaction;
import com.pmp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt WHERE pt.user = :user AND pt.type = 'EARN'")
    Integer sumEarnedByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt WHERE pt.user = :user AND pt.type = 'CONSUME'")
    Integer sumConsumedByUser(@Param("user") User user);
}
```

- [ ] **Step 6: 提交 Repository 代码**

运行: `git add src/main/java/com/pmp/repository/`
运行: `git commit -m "feat: add repository interfaces for data access"`

---

## Task 4: 创建 DTO 类

**文件:**
- 创建: `src/main/java/com/pmp/dto/LoginRequest.java`
- 创建: `src/main/java/com/pmp/dto/LoginResponse.java`
- 创建: `src/main/java/com/pmp/dto/ProjectRequest.java`
- 创建: `src/main/java/com/pmp/dto/ProjectResponse.java`
- 创建: `src/main/java/com/pmp/dto/AssignmentRequest.java`
- 创建: `src/main/java/com/pmp/dto/AssignmentResponse.java`
- 创建: `src/main/java/com/pmp/dto/UserRequest.java`
- 创建: `src/main/java/com/pmp/dto/UserResponse.java`
- 创建: `src/main/java/com/pmp/dto/TaskCompleteRequest.java`
- 创建: `src/main/java/com/pmp/dto/PointsConsumeRequest.java`

- [ ] **Step 1: 创建 LoginRequest**

```java
package com.pmp.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
```

- [ ] **Step 2: 创建 LoginResponse**

```java
package com.pmp.dto;

import com.pmp.enumeration.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private Role role;
    private String message;
}
```

- [ ] **Step 3: 创建 ProjectRequest**

```java
package com.pmp.dto;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectRequest {
    private String name;
    private ProjectType type;
    private BigDecimal unitPrice;
    private RepeatType repeatType;
    private Integer repeatDay;
    private Integer pointsToConsume;
}
```

- [ ] **Step 4: 创建 ProjectResponse**

```java
package com.pmp.dto;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private ProjectType type;
    private BigDecimal unitPrice;
    private RepeatType repeatType;
    private Integer repeatDay;
    private Integer pointsToConsume;
    private Long createdBy;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 创建 AssignmentRequest**

```java
package com.pmp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignmentRequest {
    private Long projectId;
    private Long workerId;
    private LocalDate assignDate;
}
```

- [ ] **Step 6: 创建 AssignmentResponse**

```java
package com.pmp.dto;

import com.pmp.enumeration.AssignmentStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignmentResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long workerId;
    private String workerName;
    private LocalDate assignDate;
    private AssignmentStatus status;
    private Integer completedQuantity;
}
```

- [ ] **Step 7: 创建 UserRequest**

```java
package com.pmp.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
}
```

- [ ] **Step 8: 创建 UserResponse**

```java
package com.pmp.dto;

import com.pmp.enumeration.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 9: 创建 TaskCompleteRequest**

```java
package com.pmp.dto;

import lombok.Data;

@Data
public class TaskCompleteRequest {
    private Integer quantity;
}
```

- [ ] **Step 10: 创建 PointsConsumeRequest**

```java
package com.pmp.dto;

import lombok.Data;

@Data
public class PointsConsumeRequest {
    private Long projectId;
    private Integer quantity;
}
```

- [ ] **Step 11: 提交 DTO 代码**

运行: `git add src/main/java/com/pmp/dto/`
运行: `git commit -m "feat: add DTO classes for API requests and responses"`

---

## Task 5: 创建异常处理类

**文件:**
- 创建: `src/main/java/com/pmp/exception/BusinessException.java`
- 创建: `src/main/java/com/pmp/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 BusinessException**

```java
package com.pmp.exception;

public class BusinessException extends RuntimeException {
    private String code;
    
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
```

- [ ] **Step 2: 创建 GlobalExceptionHandler**

```java
package com.pmp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "INTERNAL_ERROR");
        response.put("message", "系统错误: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

- [ ] **Step 3: 提交异常处理代码**

运行: `git add src/main/java/com/pmp/exception/`
运行: `git commit -m "feat: add exception handling classes"`

---

## Task 6: 创建 UserService

**文件:**
- 创建: `src/main/java/com/pmp/service/UserService.java`

- [ ] **Step 1: 创建 UserService 接口和实现**

```java
package com.pmp.service;

import com.pmp.dto.UserRequest;
import com.pmp.dto.UserResponse;
import com.pmp.entity.User;
import com.pmp.enumeration.Role;
import com.pmp.exception.BusinessException;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.WORKER);
        
        user = userRepository.save(user);
        return toResponse(user);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return toResponse(user);
    }
    
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getWorkers() {
        return userRepository.findByRole(Role.WORKER).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("CANNOT_DELETE_ADMIN", "不能删除管理员账号");
        }
        
        userRepository.delete(user);
    }
    
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
```

- [ ] **Step 2: 提交 UserService 代码**

运行: `git add src/main/java/com/pmp/service/UserService.java`
运行: `git commit -m "feat: add UserService with user management logic"`

---

## Task 7: 创建 ProjectService

**文件:**
- 创建: `src/main/java/com/pmp/service/ProjectService.java`

- [ ] **Step 1: 创建 ProjectService**

```java
package com.pmp.service;

import com.pmp.dto.ProjectRequest;
import com.pmp.dto.ProjectResponse;
import com.pmp.entity.Project;
import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    public ProjectResponse createProject(ProjectRequest request, Long createdBy) {
        validateProjectRequest(request);
        
        Project project = new Project();
        project.setName(request.getName());
        project.setType(request.getType());
        project.setRepeatType(request.getRepeatType());
        project.setCreatedBy(createdBy);
        
        if (request.getType() == ProjectType.RATE_BASED) {
            if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_UNIT_PRICE", "单价必须大于0");
            }
            project.setUnitPrice(request.getUnitPrice());
        }
        
        if (request.getType() == ProjectType.POINT_CONSUMING) {
            if (request.getPointsToConsume() == null || request.getPointsToConsume() <= 0) {
                throw new BusinessException("INVALID_POINTS", "消耗积分数必须大于0");
            }
            project.setPointsToConsume(request.getPointsToConsume());
        }
        
        if (request.getRepeatType() == RepeatType.WEEKLY) {
            if (request.getRepeatDay() == null || request.getRepeatDay() < 1 || request.getRepeatDay() > 7) {
                throw new BusinessException("INVALID_REPEAT_DAY", "重复星期必须在1-7之间");
            }
            project.setRepeatDay(request.getRepeatDay());
        }
        
        project = projectRepository.save(project);
        return toResponse(project);
    }
    
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long updatedBy) {
        validateProjectRequest(request);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        
        if (!project.getCreatedBy().equals(updatedBy)) {
            throw new BusinessException("NO_PERMISSION", "无权限修改此项目");
        }
        
        project.setName(request.getName());
        project.setType(request.getType());
        project.setRepeatType(request.getRepeatType());
        
        if (request.getType() == ProjectType.RATE_BASED) {
            if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_UNIT_PRICE", "单价必须大于0");
            }
            project.setUnitPrice(request.getUnitPrice());
        }
        
        if (request.getType() == ProjectType.POINT_CONSUMING) {
            if (request.getPointsToConsume() == null || request.getPointsToConsume() <= 0) {
                throw new BusinessException("INVALID_POINTS", "消耗积分数必须大于0");
            }
            project.setPointsToConsume(request.getPointsToConsume());
        }
        
        if (request.getRepeatType() == RepeatType.WEEKLY) {
            if (request.getRepeatDay() == null || request.getRepeatDay() < 1 || request.getRepeatDay() > 7) {
                throw new BusinessException("INVALID_REPEAT_DAY", "重复星期必须在1-7之间");
            }
            project.setRepeatDay(request.getRepeatDay());
        }
        
        project = projectRepository.save(project);
        return toResponse(project);
    }
    
    public List<ProjectResponse> getProjectsByCreator(Long createdBy) {
        return projectRepository.findByCreatedBy(createdBy).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        return toResponse(project);
    }
    
    @Transactional
    public void deleteProject(Long id, Long deletedBy) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        
        if (!project.getCreatedBy().equals(deletedBy)) {
            throw new BusinessException("NO_PERMISSION", "无权限删除此项目");
        }
        
        projectRepository.delete(project);
    }
    
    private void validateProjectRequest(ProjectRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException("INVALID_NAME", "项目名称不能为空");
        }
        
        if (request.getType() == null) {
            throw new BusinessException("INVALID_TYPE", "项目类型不能为空");
        }
        
        if (request.getRepeatType() == null) {
            throw new BusinessException("INVALID_REPEAT_TYPE", "重复方式不能为空");
        }
    }
    
    private ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setType(project.getType());
        response.setUnitPrice(project.getUnitPrice());
        response.setRepeatType(project.getRepeatType());
        response.setRepeatDay(project.getRepeatDay());
        response.setPointsToConsume(project.getPointsToConsume());
        response.setCreatedBy(project.getCreatedBy());
        response.setCreatedAt(project.getCreatedAt());
        return response;
    }
}
```

- [ ] **Step 2: 提交 ProjectService 代码**

运行: `git add src/main/java/com/pmp/service/ProjectService.java`
运行: `git commit -m "feat: add ProjectService with project management logic"`

---

## Task 8: 创建 AssignmentService

**文件:**
- 创建: `src/main/java/com/pmp/service/AssignmentService.java`

- [ ] **Step 1: 创建 AssignmentService**

```java
package com.pmp.service;

import com.pmp.dto.AssignmentRequest;
import com.pmp.dto.AssignmentResponse;
import com.pmp.entity.Project;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.User;
import com.pmp.exception.BusinessException;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    
    private final AssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    
    public AssignmentResponse assignProject(AssignmentRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        
        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 检查是否已经分配过
        if (assignmentRepository.findByWorkerAndProjectAndAssignDate(
                worker, project, request.getAssignDate()).isPresent()) {
            throw new BusinessException("ALREADY_ASSIGNED", "该项目已分配给该用户");
        }
        
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setWorker(worker);
        assignment.setAssignDate(request.getAssignDate());
        
        assignment = assignmentRepository.save(assignment);
        return toResponse(assignment);
    }
    
    public List<AssignmentResponse> getAssignmentsByWorkerAndDate(Long workerId, LocalDate date) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        return assignmentRepository.findByWorkerAndAssignDate(worker, date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<AssignmentResponse> getActiveAssignmentsByDate(LocalDate date) {
        return assignmentRepository.findActiveAssignmentsByDate(date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<AssignmentResponse> getAssignmentsByWorker(Long workerId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        return assignmentRepository.findByWorker(worker).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void completeAssignment(Long assignmentId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "完成数量必须大于0");
        }
        
        ProjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException("ASSIGNMENT_NOT_FOUND", "分配不存在"));
        
        assignment.setCompletedQuantity(assignment.getCompletedQuantity() + quantity);
        assignmentRepository.save(assignment);
    }
    
    private AssignmentResponse toResponse(ProjectAssignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setProjectId(assignment.getProject().getId());
        response.setProjectName(assignment.getProject().getName());
        response.setWorkerId(assignment.getWorker().getId());
        response.setWorkerName(assignment.getWorker().getUsername());
        response.setAssignDate(assignment.getAssignDate());
        response.setStatus(assignment.getStatus());
        response.setCompletedQuantity(assignment.getCompletedQuantity());
        return response;
    }
}
```

- [ ] **Step 2: 提交 AssignmentService 代码**

运行: `git add src/main/java/com/pmp/service/AssignmentService.java`
运行: `git commit -m "feat: add AssignmentService with assignment management logic"`

---

## Task 9: 创建 TaskExecutionService

**文件:**
- 创建: `src/main/java/com/pmp/service/TaskExecutionService.java`

- [ ] **Step 1: 创建 TaskExecutionService**

```java
package com.pmp.service;

import com.pmp.dto.TaskCompleteRequest;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.Project;
import com.pmp.entity.TaskExecution;
import com.pmp.enumeration.ProjectType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.TaskExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TaskExecutionService {
    
    private final TaskExecutionRepository taskExecutionRepository;
    private final AssignmentRepository assignmentRepository;
    private final PointsService pointsService;
    
    @Transactional
    public void completeTask(Long assignmentId, TaskCompleteRequest request, Long userId) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "完成数量必须大于0");
        }
        
        ProjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException("ASSIGNMENT_NOT_FOUND", "分配不存在"));
        
        // 检查权限
        if (!assignment.getWorker().getId().equals(userId)) {
            throw new BusinessException("NO_PERMISSION", "无权限执行此任务");
        }
        
        Project project = assignment.getProject();
        
        // 创建任务执行记录
        TaskExecution execution = new TaskExecution();
        execution.setAssignment(assignment);
        execution.setExecutionDate(LocalDate.now());
        execution.setQuantity(request.getQuantity());
        
        // 计算积分
        if (project.getType() == ProjectType.RATE_BASED) {
            int pointsEarned = project.getUnitPrice().multiply(
                    BigDecimal.valueOf(request.getQuantity())).intValue();
            execution.setPointsEarned(pointsEarned);
            
            // 增加用户积分
            pointsService.addPoints(assignment.getWorker().getId(), pointsEarned, 
                    "完成任务: " + project.getName());
        }
        
        taskExecutionRepository.save(execution);
        
        // 更新分配完成数量
        assignment.setCompletedQuantity(assignment.getCompletedQuantity() + request.getQuantity());
        assignmentRepository.save(assignment);
    }
    
    public java.util.List<TaskExecution> getTaskExecutionsByUser(Long userId) {
        return taskExecutionRepository.findByAssignmentWorkerOrderByExecutionDateDesc(
                assignmentRepository.getReferenceById(userId).getWorker());
    }
}
```

- [ ] **Step 2: 提交 TaskExecutionService 代码**

运行: `git add src/main/java/com/pmp/service/TaskExecutionService.java`
运行: `git commit -m "feat: add TaskExecutionService with task execution logic"`

---

## Task 10: 创建 PointsService

**文件:**
- 创建: `src/main/java/com/pmp/service/PointsService.java`

- [ ] **Step 1: 创建 PointsService**

```java
package com.pmp.service;

import com.pmp.dto.PointsConsumeRequest;
import com.pmp.entity.PointsTransaction;
import com.pmp.entity.Project;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.TaskExecution;
import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.TransactionType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.PointsTransactionRepository;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.TaskExecutionRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {
    
    private final PointsTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    
    @Transactional
    public void addPoints(Long userId, Integer amount, String description) {
        com.pmp.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setType(TransactionType.EARN);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        
        transactionRepository.save(transaction);
    }
    
    @Transactional
    public void consumePoints(Long userId, Long projectId, Integer quantity) {
        com.pmp.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        
        if (project.getType() != ProjectType.POINT_CONSUMING) {
            throw new BusinessException("INVALID_PROJECT_TYPE", "该项目不是积分消耗类型");
        }
        
        Integer totalPointsToConsume = project.getPointsToConsume() * quantity;
        Integer currentBalance = getUserPointsBalance(userId);
        
        if (currentBalance < totalPointsToConsume) {
            throw new BusinessException("INSUFFICIENT_POINTS", 
                    "积分不足，当前余额: " + currentBalance + "，需要: " + totalPointsToConsume);
        }
        
        // 创建积分消耗记录
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setType(TransactionType.CONSUME);
        transaction.setAmount(totalPointsToConsume);
        transaction.setDescription("执行项目: " + project.getName());
        transactionRepository.save(transaction);
        
        // 创建任务执行记录
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setWorker(user);
        assignment.setAssignDate(LocalDate.now());
        assignmentRepository.save(assignment);
        
        TaskExecution execution = new TaskExecution();
        execution.setAssignment(assignment);
        execution.setExecutionDate(LocalDate.now());
        execution.setQuantity(quantity);
        execution.setPointsConsumed(totalPointsToConsume);
        taskExecutionRepository.save(execution);
    }
    
    public Integer getUserPointsBalance(Long userId) {
        com.pmp.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        Integer earned = transactionRepository.sumEarnedByUser(user);
        Integer consumed = transactionRepository.sumConsumedByUser(user);
        
        return (earned != null ? earned : 0) - (consumed != null ? consumed : 0);
    }
    
    public List<PointsTransaction> getUserTransactions(Long userId) {
        com.pmp.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Project> getPointConsumingProjects() {
        return projectRepository.findAll().stream()
                .filter(p -> p.getType() == ProjectType.POINT_CONSUMING)
                .collect(java.util.stream.Collectors.toList());
    }
}
```

- [ ] **Step 2: 提交 PointsService 代码**

运行: `git add src/main/java/com/pmp/service/PointsService.java`
运行: `git commit -m "feat: add PointsService with points management logic"`

---
