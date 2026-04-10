package com.priceradar.common.result;

import lombok.Getter;

/**
 * 结果码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_EXISTS(1001, "用户已存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    ACCOUNT_LOCKED(1003, "账号已被锁定"),

    PRODUCT_NOT_FOUND(2001, "商品不存在"),

    PRICE_FETCH_FAILED(3001, "价格获取失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
