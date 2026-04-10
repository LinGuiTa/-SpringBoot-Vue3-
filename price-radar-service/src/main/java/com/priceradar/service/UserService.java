package com.priceradar.service;

import com.priceradar.domain.dto.LoginDTO;
import com.priceradar.domain.dto.RegisterDTO;
import com.priceradar.domain.dto.UpdatePasswordDTO;
import com.priceradar.domain.entity.User;
import com.priceradar.domain.vo.LoginVO;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto, String ip);

    void logout(String token);

    User getCurrentUser(Long userId);

    void updateProfile(Long userId, User user);

    void updatePassword(Long userId, UpdatePasswordDTO dto);
}
