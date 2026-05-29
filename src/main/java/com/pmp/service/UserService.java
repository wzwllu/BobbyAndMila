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

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
}
