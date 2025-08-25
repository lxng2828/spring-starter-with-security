package com.vtlong.my_spring_boot_project.model;

public enum RoleType {
    ADMIN("ADMIN", "Administrator"),
    MODERATOR("MODERATOR", "Moderator"),
    USER("USER", "Regular User");

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType fromCode(String code) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getCode().equalsIgnoreCase(code)) {
                return roleType;
            }
        }
        throw new IllegalArgumentException("Invalid role type code: " + code);
    }
}
