package com.courage.streamer.api.dto;

import com.courage.streamer.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPageDto {
        private User user;
        private VideoPageResponse videos;
}
