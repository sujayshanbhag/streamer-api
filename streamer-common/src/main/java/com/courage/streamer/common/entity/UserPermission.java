package com.courage.streamer.common.entity;

import com.courage.streamer.common.exception.enums.PermissionType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_permissions")
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType type;
}
