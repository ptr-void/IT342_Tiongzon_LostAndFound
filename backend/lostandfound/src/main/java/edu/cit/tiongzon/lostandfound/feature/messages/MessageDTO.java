package edu.cit.tiongzon.lostandfound.feature.messages;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private Long itemId;
    private String itemTitle;
    private String content;
    private int senderWarningMarks;
    private String senderAvatar;
    private LocalDateTime createdAt;
}
