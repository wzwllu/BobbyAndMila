package com.pmp.service;

import com.pmp.dto.DailyStatsResponse;
import com.pmp.dto.LoginRequest;
import com.pmp.dto.LoginResponse;
import com.pmp.dto.TaskStatsResponse;
import com.pmp.dto.UserRequest;
import com.pmp.dto.UserResponse;
import com.pmp.dto.UserStatsResponse;
import com.pmp.entity.User;
import com.pmp.enumeration.Role;
import com.pmp.exception.BusinessException;
import com.pmp.repository.PointsTransactionRepository;
import com.pmp.repository.TaskExecutionRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskExecutionRepository taskExecutionRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    /**
     * 创建用户
     *
     * @param request 用户请求对象
     * @return 创建的用户响应
     * @throws BusinessException 当用户名已存在时抛出异常
     */
    public UserResponse createUser(UserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }

        // 创建新用户，默认角色为WORKER
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.WORKER);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户响应
     * @throws BusinessException 当用户不存在时抛出异常
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return toResponse(user);
    }

    /**
     * 获取所有用户
     *
     * @return 用户响应列表
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有工人
     *
     * @return 工人用户响应列表
     */
    public List<UserResponse> getWorkers() {
        List<User> workers = userRepository.findByRole(Role.WORKER);
        return workers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户响应
     * @throws BusinessException 当用户不存在时抛出异常
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return toResponse(user);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @throws BusinessException 当用户不存在或为管理员时抛出异常
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        // 不允许删除管理员
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("CANNOT_DELETE_ADMIN", "不能删除管理员用户");
        }

        userRepository.deleteById(id);
    }

    /**
     * 管理员重置用户密码
     *
     * @param id          用户ID
     * @param newPassword 新密码（明文，方法内加密）
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BusinessException("INVALID_PASSWORD", "密码长度至少6位");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 获取用户统计数据
     */
    public UserStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        UserStatsResponse stats = new UserStatsResponse();
        stats.setUserId(user.getId());
        stats.setUsername(user.getUsername());
        stats.setCompletedTasks(taskExecutionRepository.countCompletedByUserId(userId));
        stats.setIncompleteTasks(taskExecutionRepository.countIncompleteByUserId(userId));
        stats.setTotalEarned(pointsTransactionRepository.sumEarnedByUser(user));
        stats.setTotalConsumed(pointsTransactionRepository.sumConsumedByUser(user));
        stats.setBalance(stats.getTotalEarned() - stats.getTotalConsumed());
        return stats;
    }

    /**
     * 获取用户按天统计
     */
    public List<DailyStatsResponse> getDailyStats(Long userId) {
        List<Object[]> rows = taskExecutionRepository.findDailyStatsByUserId(userId);
        return rows.stream().map(r -> {
            DailyStatsResponse d = new DailyStatsResponse();
            d.setDate((java.time.LocalDate) r[0]);
            d.setCompletedCount(((Number) r[1]).longValue());
            d.setEarnedPoints(((Number) r[2]).longValue());
            d.setConsumedPoints(((Number) r[3]).longValue());
            return d;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取用户按任务统计
     */
    public List<TaskStatsResponse> getTaskStats(Long userId) {
        List<Object[]> rows = taskExecutionRepository.findTaskStatsByUserId(userId);
        return rows.stream().map(r -> {
            TaskStatsResponse t = new TaskStatsResponse();
            t.setProjectId(((Number) r[0]).longValue());
            t.setProjectName((String) r[1]);
            t.setProjectType(r[2] != null ? r[2].toString() : "");
            t.setCompletedCount(((Number) r[3]).longValue());
            t.setIncompleteCount(((Number) r[4]).longValue());
            t.setTotalPoints(((Number) r[5]).longValue());
            return t;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 将User实体转换为UserResponse DTO
     *
     * @param user 用户实体
     * @return 用户响应DTO
     */
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "密码错误");
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setMessage("登录成功");
        return response;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
