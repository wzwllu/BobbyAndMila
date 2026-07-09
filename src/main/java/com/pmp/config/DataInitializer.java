package com.pmp.config;

import com.pmp.entity.Project;
import com.pmp.entity.User;
import com.pmp.enumeration.ProjectStatus;
import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import com.pmp.enumeration.Role;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 数据初始化器
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员账户
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("默认管理员账户已创建: admin/admin123");
        }

        // 首次启动时创建示例任务（一个增加积分、一个消耗积分）
        if (projectRepository.count() == 0) {
            Project earnProject = new Project();
            earnProject.setName("日常数据录入");
            earnProject.setType(ProjectType.EARN);
            earnProject.setUnitPrice(new BigDecimal("10"));
            earnProject.setRepeatType(RepeatType.DAILY);
            earnProject.setStatus(ProjectStatus.ACTIVE);
            projectRepository.save(earnProject);

            Project consumeProject = new Project();
            consumeProject.setName("积分商城兑换");
            consumeProject.setType(ProjectType.CONSUME);
            consumeProject.setPointsToConsume(50);
            consumeProject.setRepeatType(RepeatType.NONE);
            consumeProject.setStatus(ProjectStatus.ACTIVE);
            projectRepository.save(consumeProject);

            System.out.println("已创建示例任务：日常数据录入（增加积分）、积分商城兑换（消耗积分）");
        }
    }
}
