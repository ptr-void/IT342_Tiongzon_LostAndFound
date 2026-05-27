package edu.cit.tiongzon.lostandfound.feature.users;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    private Role role;
    private boolean active;
    private int warningMarks;
    private boolean banned;
    private LocalDateTime createdAt;
}
