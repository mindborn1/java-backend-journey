package com.mindborn.day08;

/**
 * Record 练习：用户信息 DTO
 */
public record UserRecord (Long id, String name, Integer age, String email) {

    public UserRecord {
        if (age < 0 || age >150) {
            throw new IllegalArgumentException("年龄不合法" + age);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
    }
}
