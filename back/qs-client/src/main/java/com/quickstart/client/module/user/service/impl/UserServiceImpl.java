package com.quickstart.client.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickstart.common.domain.user.dto.*;
import com.quickstart.common.domain.user.User;
import com.quickstart.client.module.user.mapper.UserMapper;
import com.quickstart.client.module.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Resource
    private UserMapper userMapper;



    @Override
    public IPage<User> pageAdminUsers(long pageNo, long pageSize, String userName, String phone, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeletedFlag, 0)
                .like(StringUtils.hasText(userName), User::getUserName, userName)
                .like(StringUtils.hasText(phone), User::getPhone, phone)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);
        return userMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
    }

    @Override
    public User findById(Long userId) {
        if (userId == null) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId)
                .eq(User::getDeletedFlag, 0));
    }

    @Override
    public User findByMemberCode(String memberCode) {
        if (!StringUtils.hasText(memberCode)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserCode, memberCode)
                .eq(User::getDeletedFlag, 0));
    }

    @Override
    public User findByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getDeletedFlag, 0));
    }

    @Override
    public User createByAdmin(AdminUserCreateRequest request) {
        User exists = findByPhone(request.getPhone());
        if (exists != null) {
            throw new IllegalArgumentException("手机号已存在");
        }
        User user = new User();
        user.setUserCode(generateNextMemberCode());
        user.setUserType(request.getUserType());
        user.setUserName(request.getUserName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setStatus(1);
        user.setDeletedFlag(0);
        user.setLoginCount(0);
        user.setMemberLevelScore(BigDecimal.ZERO);
        user.setCreditScore(0);
        user.setRegisterSource(3);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User registerClient(ClientRegisterDTO request) {
        User exists = findByPhone(request.getPhone());
        if (exists != null) {
            throw new IllegalArgumentException("手机号已注册");
        }
        User user = new User();
        user.setUserCode(generateNextMemberCode());
        user.setUserType(2);
        user.setUserName(request.getUserName());
        user.setPhone(request.getPhone());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setRegisterSource(request.getRegisterSource());
        user.setStatus(1);
        user.setDeletedFlag(0);
        user.setLoginCount(0);
        user.setMemberLevelScore(BigDecimal.ZERO);
        user.setCreditScore(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User adminLogin(AdminLoginRequest request) {
        User user = findByPhone(request.getPhone());
        if (user == null || user.getUserType() == null || user.getUserType() != 1) {
            throw new IllegalArgumentException("管理员账号不存在");
        }
        if (!PASSWORD_ENCODER.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        return user;
    }

    @Override
    public User clientLogin(ClientLoginDTO request) {
        User user = findByPhone(request.getPhone());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!PASSWORD_ENCODER.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        return user;
    }

    @Override
    public boolean updateStatusByMemberCode(String memberCode, Integer status) {
        User user = findByMemberCode(memberCode);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public void updateInfo(String userCode, UpdateInfoDTO updateInfoDTO) {

    }

    private String generateNextMemberCode() {
        Long currentMax = userMapper.selectMaxMemberCodeNumber();
        long nextNumber = (currentMax == null ? 0 : currentMax + 1);
        int digit = Math.max(6, String.valueOf(nextNumber).length());
        return "QS" + String.format("%0" + digit + "d", nextNumber);
    }
}
