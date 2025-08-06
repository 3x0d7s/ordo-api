package com.kyut.ordo.core.common.role;

import java.util.HashMap;
import java.util.Map;

public interface RoleFactory<T, S> {
    T createOwnerRole(S entity);
    T createMemberRole(S entity);
    T createGuestRole(S entity);

    default Map<String, T> rolesAsMap(S entity) {
        Map<String, T> roles = new HashMap<>();

        roles.put("Owner", createOwnerRole(entity));
        roles.put("Member", createMemberRole(entity));
        roles.put("Guest", createGuestRole(entity));

        return roles;
    }
}
