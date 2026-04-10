package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String username;

    private String role;

    private String avatar;
}
