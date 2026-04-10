package com.courage.streamer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.courage.streamer.common.exception.enums.PermissionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionsDto {
    private Long id;
    private Boolean isGuest;
    private List<PermissionType> permissions;
}
