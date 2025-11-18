package com.courage.streamer.api.model.entity;

import com.courage.streamer.api.model.enums.PermissionType;
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
