package com.project.schoolmanagment.entity.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMIN("Admin"),
    STUDENT("Student"),
    TEACHER("Teacher"),
    MANAGER("Dean"),
    ASSISTANT_MANAGER("ViceDean");

    public final String name;

    RoleType(String name) {
        this.name = name;
    }
}
