package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.priceradar.common.exception.BusinessException;
import com.priceradar.common.result.ResultCode;
import com.priceradar.domain.dto.LoginDTO;
import com.priceradar.domain.dto.RegisterDTO;
import com.priceradar.domain.dto.UpdatePasswordDTO;
import com.priceradar.domain.entity.User;
import com.priceradar.domain.vo.LoginVO;
import com.priceradar.mapper.UserMapper;
import com.priceradar.service.UserService;
import com.priceradar.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto, String ip) {
        String lockKey = "login:lock:" + dto.getUsername();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            String failKey = "login:fail:" + dto.getUsername();
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, 900, TimeUnit.SECONDS);
            if (failCount != null && failCount >= 5) {
                redisTemplate.opsForValue().set(lockKey, "1", 900, TimeUnit.SECONDS);
                redisTemplate.delete(failKey);
            }
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(1003, "账号已被禁用");
        }

        redisTemplate.delete("login:fail:" + dto.getUsername());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    @Override
    public void logout(String token) {
        String blacklistKey = "login:blacklist:" + token;
        redisTemplate.opsForValue().set(blacklistKey, "1", 7200, TimeUnit.SECONDS);
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateProfile(Long userId, User user) {
        User update = new User();
        update.setId(userId);
        update.setEmail(user.getEmail());
        update.setPhone(user.getPhone());
        update.setAvatar(user.getAvatar());
        userMapper.updateById(update);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }
}
