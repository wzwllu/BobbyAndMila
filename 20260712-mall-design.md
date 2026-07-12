# 积分商城设计方案

---

## 一、设计目标

### 1.1 解决了什么

| 当前问题 | 改造后 |
|---------|--------|
| 消耗积分由管理员创建 Project 再 Assignment，流程重、概念抽象 | 家长直接在商城上架奖励，孩子像逛商城一样浏览兑换 |
| 孩子提交消耗无需审核，直接扣分，家长无法管控 | 孩子提交兑换申请 → 家长审核 → 审核通过才扣分 |
| 没有奖励清单，孩子不知道能兑换什么 | 商城页面清晰展示所有可用奖励及所需积分 |
| 没有兑换历史，家长孩子都不知道换了什么 | 完整的兑换记录 + 状态追踪 |

### 1.2 核心流程

```
家长端                    孩子端                   系统
  │                        │                      │
  ├─ 上架/下架奖励 ──────→ │                      │
  │                        ├─ 浏览商城 ─────────→ │
  │                        ├─ 提交兑换申请 ─────→ │
  │                        │                      ├─ 余额校验
  │                        │                      ├─ 冻结积分
  │                        │                      └─ 状态: PENDING
  │                        │                      │
  ├─ 审核兑换申请 ──────→ │                      │
  │  ├─ 批准 ────────────→ │                      ├─ 扣减积分
  │  │                     │                      ├─ 创建交易记录
  │  │                     │                      └─ 状态: APPROVED
  │  └─ 拒绝 ────────────→ │                      └─ 状态: REJECTED，解冻
```

---

## 二、新增模型

### 2.1 奖励实体 (Reward)

```java
package com.pmp.entity;

@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 奖励名称（如"看30分钟动画片"、"去游乐园"） */
    @Column(nullable = false)
    private String name;

    /** 所需积分 */
    @Column(nullable = false)
    private Integer costPoints;

    /** 说明（可描述兑换规则、使用方式等） */
    @Column(length = 500)
    private String description;

    /** 图标/图片 URL（可选） */
    private String imageUrl;

    /** 库存（null 表示不限量） */
    private Integer stock;

    /** 每日限兑次数（null 表示不限） */
    @Column(name = "max_per_day")
    private Integer maxPerDay;

    /** 上架状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardStatus status;

    /** 创建人（家长） */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = RewardStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 2.2 奖励状态枚举

```java
package com.pmp.enumeration;

public enum RewardStatus {
    ACTIVE("上架中"),
    DISABLED("已下架");

    private final String label;
}
```

### 2.3 兑换申请实体 (Redemption)

```java
package com.pmp.entity;

@Entity
@Table(name = "redemptions")
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 兑换的孩子 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 兑换的奖励 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    /** 兑换数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 消耗的总积分（快照，reward.costPoints * quantity） */
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    /** 兑换时填写的备注/留言 */
    @Column(length = 500)
    private String remark;

    /** 审核状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RedemptionStatus status;

    /** 审核人（家长） */
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /** 审核备注/拒绝原因 */
    @Column(name = "review_remark", length = 500)
    private String reviewRemark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = RedemptionStatus.PENDING;
    }
}
```

### 2.4 兑换状态枚举

```java
package com.pmp.enumeration;

public enum RedemptionStatus {
    PENDING("待审核"),
    APPROVED("已通过，积分已扣除"),
    REJECTED("已拒绝");

    private final String label;
}
```

---

## 三、DTO 层

### 3.1 RewardRequest（家长创建/编辑奖励）

```java
@Data
public class RewardRequest {
    @NotBlank(message = "奖励名称不能为空")
    @Size(max = 100)
    private String name;

    @NotNull(message = "所需积分不能为空")
    @Positive(message = "所需积分必须大于 0")
    private Integer costPoints;

    @Size(max = 500)
    private String description;

    private String imageUrl;

    @Min(1)
    private Integer stock;

    @Min(1)
    private Integer maxPerDay;
}
```

### 3.2 RewardResponse（返回给前端）

```java
@Data
public class RewardResponse {
    private Long id;
    private String name;
    private Integer costPoints;
    private String description;
    private String imageUrl;
    private Integer stock;
    private Integer maxPerDay;
    private String status;
    private String statusLabel;
    private Long createdBy;
    private LocalDateTime createdAt;
}
```

### 3.3 RedemptionRequest（孩子提交兑换）

```java
@Data
public class RedemptionRequest {
    @NotNull(message = "请选择要兑换的奖励")
    private Long rewardId;

    @Min(value = 1, message = "兑换数量至少为 1")
    private Integer quantity = 1;

    @Size(max = 500)
    private String remark;
}
```

### 3.4 RedemptionResponse

```java
@Data
public class RedemptionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long rewardId;
    private String rewardName;
    private Integer costPoints;
    private Integer quantity;
    private Integer totalPoints;
    private String remark;
    private String status;
    private String statusLabel;
    private String reviewRemark;
    private LocalDateTime createdAt;
}
```

### 3.5 ReviewRedemptionRequest（家长审核）

```java
@Data
public class ReviewRedemptionRequest {
    @NotNull
    private Boolean approved;
    private String reviewRemark;  // 拒绝原因
}
```

---

## 四、Repository 层

```java
@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    /** 获取所有上架中的奖励 */
    List<Reward> findByStatus(RewardStatus status);

    /** 家长查看所有奖励（含已下架） */
    Page<Reward> findAll(Pageable pageable);

    /** 创建人查看自己的奖励 */
    List<Reward> findByCreatedBy(Long createdBy);
}

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    /** 孩子的兑换记录 */
    Page<Redemption> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 所有待审核的兑换 */
    List<Redemption> findByStatus(RedemptionStatus status);
    Page<Redemption> findByStatus(RedemptionStatus status, Pageable pageable);

    /** 所有兑换记录（家长查看） */
    Page<Redemption> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 检查孩子当日已兑换次数（用于限兑） */
    long countByUserIdAndRewardIdAndCreatedAtBetween(Long userId, Long rewardId,
                                                     LocalDateTime start, LocalDateTime end);

    /** 统计孩子待审核的兑换数 */
    boolean existsByUserIdAndStatus(Long userId, RedemptionStatus status);
}
```

---

## 五、Service 层

### 5.1 RewardService

```java
@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    /** 家长创建奖励 */
    @Transactional
    public RewardResponse createReward(RewardRequest request, Long parentId);

    /** 家长编辑奖励 */
    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request);

    /** 家长上架/下架 */
    @Transactional
    public void toggleStatus(Long id);

    /** 孩子浏览商城（仅上架中的） */
    public List<RewardResponse> listActive();

    /** 家长管理全部奖励 */
    public Page<RewardResponse> listAll(Pageable pageable);

    /** 奖励详情 */
    public RewardResponse getReward(Long id);
}
```

### 5.2 RedemptionService

```java
@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final RedemptionRepository redemptionRepository;
    private final RewardRepository rewardRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final UserRepository userRepository;

    /**
     * 孩子提交兑换申请
     * 1. 校验奖励存在且上架中
     * 2. 校验库存
     * 3. 校验每日限兑
     * 4. 校验余额充足
     * 5. 创建 Redemption（状态 PENDING，不扣分）
     */
    @Transactional
    public void submitRedemption(Long userId, RedemptionRequest request);

    /**
     * 家长审核兑换
     * 批准：扣除积分（创建 PointsTransaction），状态 → APPROVED
     * 拒绝：状态 → REJECTED，不扣分
     */
    @Transactional
    public void reviewRedemption(Long redemptionId, Long parentId, ReviewRedemptionRequest request);

    /** 孩子的兑换记录 */
    public Page<RedemptionResponse> getMyRedemptions(Long userId, Pageable pageable);

    /** 家长查看所有兑换记录 */
    public Page<RedemptionResponse> getAllRedemptions(Pageable pageable);

    /** 家长查看待审核兑换 */
    public List<RedemptionResponse> getPendingRedemptions();
}
```

---

## 六、Controller 层

### 6.1 家长端 (ParentController)

| 方法 | 路由 | 说明 |
|------|------|------|
| GET | /parent/mall | 商城管理页面（Thymeleaf） |
| GET | /parent/mall/rewards | 奖励列表（JSON，含分页） |
| POST | /parent/mall/rewards | 创建奖励 |
| PUT | /parent/mall/rewards/{id} | 编辑奖励 |
| PUT | /parent/mall/rewards/{id}/toggle | 上架/下架切换 |
| GET | /parent/mall/redemptions | 兑换审核页面 |
| GET | /parent/mall/redemptions/list | 兑换列表（JSON） |
| POST | /parent/mall/redemptions/{id}/review | 审核兑换 |

### 6.2 孩子端 (ChildController)

| 方法 | 路由 | 说明 |
|------|------|------|
| GET | /child/mall | 积分商城页面 |
| GET | /child/mall/rewards | 可用奖励列表（JSON） |
| POST | /child/mall/redeem | 提交兑换申请 |
| GET | /child/mall/redemptions | 我的兑换记录 |

---

## 七、积分交易联动

审核通过时创建 `PointsTransaction`：

```java
// RedemptionService.reviewRedemption — 批准分支
PointsTransaction tx = new PointsTransaction();
tx.setUser(redemption.getUser());
tx.setAmount(redemption.getTotalPoints());
tx.setType(TransactionType.CONSUME);
tx.setDescription("兑换: " + redemption.getReward().getName()
                  + " x" + redemption.getQuantity());
tx.setCreatedAt(LocalDateTime.now());
pointsTransactionRepository.save(tx);
```

**注意：** 此处 PointsTransaction 仅用于最终入账，不在提交时创建。与原有 `submitConsume` 立即扣分的逻辑不同，改为审核后才实际扣分。

原有 `submitConsume`（`TaskService.submitConsume`）和 `PointsConsumeRequest` 在新商城上线后**废弃**，不再使用。

---

## 八、现有代码变更清单

### 8.1 新增文件

| 文件 | 类型 |
|------|------|
| `entity/Reward.java` | 新实体 |
| `entity/Redemption.java` | 新实体 |
| `enumeration/RewardStatus.java` | 新枚举 |
| `enumeration/RedemptionStatus.java` | 新枚举 |
| `repository/RewardRepository.java` | 新 Repository |
| `repository/RedemptionRepository.java` | 新 Repository |
| `service/RewardService.java` | 新 Service |
| `service/RedemptionService.java` | 新 Service |
| `dto/reward/RewardRequest.java` | 新 DTO |
| `dto/reward/RewardResponse.java` | 新 DTO |
| `dto/redemption/RedemptionRequest.java` | 新 DTO |
| `dto/redemption/RedemptionResponse.java` | 新 DTO |
| `dto/redemption/ReviewRedemptionRequest.java` | 新 DTO |
| `templates/parent/mall.html` | 商城管理页 |
| `templates/parent/mall-redemptions.html` | 兑换审核页 |
| `templates/child/mall.html` | 孩子商城页 |
| `templates/child/mall-redemptions.html` | 兑换记录页 |
| `controller/ParentMallController.java` | 家长端商城控制器 |
| `controller/ChildMallController.java` | 孩子端商城控制器 |

### 8.2 修改文件

| 文件 | 变更 |
|------|------|
| `ParentController.java` | 添加商城页面路由导航 |
| `ChildController.java` | 添加商城页面路由导航 |
| `SecurityConfig.java` | 添加 `/parent/mall/**` 和 `/child/mall/**` 权限配置 |
| `templates/parent/index.html` | 导航栏添加"积分商城"入口 |
| `templates/child/index.html` | 导航栏添加"积分商城"入口 |
| `TaskService.java` | 标记 `submitConsume` 为废弃 (`@Deprecated`) |

### 8.3 删除文件

| 文件 | 说明 |
|------|------|
| `dto/PointsConsumeRequest.java` | 被 `RedemptionRequest` 取代 |
| `worker/consume.html` | 被商城页面取代 |

---

## 九、页面交互设计

### 9.1 孩子端商城

```
┌─────────────────────────────────────────┐
│  🏪 积分商城                   余额: 150 │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │ 🎮       │  │ 📺       │  │ 🍦    │ │
│  │ 玩游戏    │  │ 看动画片  │  │ 吃冰淇淋│ │
│  │ 30分钟    │  │ 30分钟   │  │       │ │
│  │          │  │          │  │       │ │
│  │ 50 积分  │  │ 30 积分  │  │ 20 积分│ │
│  │ [兑换]   │  │ [兑换]   │  │ [兑换] │ │
│  └──────────┘  └──────────┘  └────────┘ │
│                                         │
│     ┌────兑换确认─────┐                 │
│     │ 确认兑换"玩游戏30分钟"？│          │
│     │ 消耗: 50 积分        │            │
│     │ 剩余: 100 积分       │            │
│     │    [取消]  [确认兑换] │            │
│     └────────────────────┘              │
│                                         │
│  [我的兑换记录]                          │
└─────────────────────────────────────────┘
```

### 9.2 家长端商城管理

```
┌─────────────────────────────────────────┐
│  ⚙️ 积分商城管理                         │
├─────────────────────────────────────────┤
│  [＋ 新增奖励]                           │
├─────────────────────────────────────────┤
│  奖励列表                                │
│  ┌─────────────────────────────────────┐│
│  │ 名称        积分  库存  状态  操作   ││
│  │─────────────────────────────────────││
│  │ 玩游戏30分  50    10    上架  [下架] ││
│  │ 看动画片30分 30    -    上架  [下架] ││
│  │ 吃冰淇淋    20    50    上架  [下架] ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌──待审核兑换 (3)───────────────────┐  │
│  │ 孩子  奖励       积分  时间   操作  │  │
│  │───────────────────────────────────│  │
│  │ 小宝  玩游戏30分  50   10:30  [✓][✗]│
│  │ 大宝  看动画片    30   11:00  [✓][✗]│
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
```

---

## 十、与旧方案的迁移策略

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1 | 新增 Reward + Redemption 实体和全部代码 | 新旧并行，互不影响 |
| 2 | 家长端导航栏增加"积分商城"入口 | 孩子端增加"积分商城"入口 |
| 3 | 标注 `submitConsume` 为废弃 | 旧的消耗功能仍然可用，但不推荐 |
| 4 | 运营过渡期（1-2周） | 引导家长将消耗类 Project 迁移为 Reward |
| 5 | 删除 `worker/consume.html` 和 `PointsConsumeRequest` | 旧功能下线 |

---

## 十一、边界情况处理

| 场景 | 处理方式 |
|------|---------|
| 孩子提交兑换后奖励被下架 | 不影响已提交的待审核记录，审核时提示"该奖励已下架，建议拒绝" |
| 余额刚好够但审核时已被其他订单消耗 | 审核时重新校验余额，不足则自动拒绝并提示"积分不足" |
| 库存不足 | 提交时校验一次，审核时再校验一次，防止超卖 |
| 每日限兑 | `maxPerDay` 以 `createdAt` 日期为准，自然日限制 |
| 孩子同时提交多个兑换 | 各自独立 PENDING，审核时逐个校验余额 |
