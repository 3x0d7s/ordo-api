package com.kyut.ordo.common.role;

public interface RoleFactory<T> {
    T createOwnerRole();
    T createMemberRole();
    T createGuestRole();
}
