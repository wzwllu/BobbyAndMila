package com.pmp.config;

import com.pmp.entity.Project;
import com.pmp.entity.Reward;
import com.pmp.entity.User;
import com.pmp.enumeration.ProjectStatus;
import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import com.pmp.enumeration.RewardStatus;
import com.pmp.enumeration.Role;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.RewardRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RewardRepository rewardRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("默认管理员账户已创建: admin/admin123");
        }

        if (!userRepository.existsByUsername("xiaobao")) {
            User child = new User();
            child.setUsername("xiaobao");
            child.setPassword(passwordEncoder.encode("123456"));
            child.setRole(Role.WORKER);
            userRepository.save(child);
            log.info("示例孩子账户已创建: xiaobao/123456");
        }

        if (projectRepository.count() == 0) {
            Project earnProject = new Project();
            earnProject.setName("日常数据录入");
            earnProject.setType(ProjectType.EARN);
            earnProject.setUnitPrice(new BigDecimal("10"));
            earnProject.setRepeatType(RepeatType.DAILY);
            earnProject.setStatus(ProjectStatus.ACTIVE);
            projectRepository.save(earnProject);
            log.info("已创建示例任务：日常数据录入（增加积分）");
        }

        if (rewardRepository.count() == 0) {
            Reward r1 = new Reward();
            r1.setName("看30分钟动画片");
            r1.setCostPoints(30);
            r1.setDescription("可以看 30 分钟喜欢的动画片");
            r1.setStock(null);
            r1.setStatus(RewardStatus.ACTIVE);
            r1.setCreatedBy(1L);
            rewardRepository.save(r1);

            Reward r2 = new Reward();
            r2.setName("吃冰淇淋");
            r2.setCostPoints(20);
            r2.setDescription("可以吃一个冰淇淋");
            r2.setStock(50);
            r2.setStatus(RewardStatus.ACTIVE);
            r2.setCreatedBy(1L);
            rewardRepository.save(r2);

            Reward r3 = new Reward();
            r3.setName("去游乐园");
            r3.setCostPoints(200);
            r3.setDescription("周末去游乐园玩一天");
            r3.setStock(5);
            r3.setStatus(RewardStatus.ACTIVE);
            r3.setCreatedBy(1L);
            rewardRepository.save(r3);

            log.info("已创建示例奖励：看动画片、吃冰淇淋、去游乐园");
        }
    }
}
