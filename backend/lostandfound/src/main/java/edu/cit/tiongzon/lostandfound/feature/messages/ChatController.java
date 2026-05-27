package edu.cit.tiongzon.lostandfound.feature.messages;

import edu.cit.tiongzon.lostandfound.feature.items.Item;
import edu.cit.tiongzon.lostandfound.feature.items.ItemRepository;
import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, MessageRepository messageRepository,
            UserRepository userRepository, ItemRepository itemRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @MessageMapping("/chat.global")
    public void sendGlobalMessage(@Payload MessageDTO chatMessage, Authentication authentication) {
        User sender = currentUser(authentication);
        if (sender == null || chatMessage.getContent() == null || chatMessage.getContent().isBlank()) return;
        Message saved = saveMessage(sender, null, null, chatMessage.getContent());
        messagingTemplate.convertAndSend("/topic/global", convertToDto(saved));
    }

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload MessageDTO chatMessage, Authentication authentication) {
        User sender = currentUser(authentication);
        if (sender == null || chatMessage.getReceiverId() == null || chatMessage.getItemId() == null) return;
        User receiver = userRepository.findById(chatMessage.getReceiverId()).orElse(null);
        Item item = itemRepository.findById(chatMessage.getItemId()).orElse(null);
        if (receiver != null && receiver.getUserId().equals(sender.getUserId())) return;
        if (receiver == null || item == null || chatMessage.getContent() == null || chatMessage.getContent().isBlank()) return;
        MessageDTO dto = convertToDto(saveMessage(sender, receiver, item, chatMessage.getContent()));
        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages", dto);
        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/messages", dto);
    }

    @GetMapping("/messages/global")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MessageDTO>> getGlobalMessages() {
        return ResponseEntity.ok(messageRepository.findByReceiverIsNullOrderByCreatedAtAsc().stream().map(this::convertToDto).toList());
    }

    @GetMapping("/messages/private/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MessageDTO>> getPrivateMessages(@PathVariable Long userId, @RequestParam Long itemId,
            Authentication authentication) {
        User currentUser = currentUser(authentication);
        User otherUser = userRepository.findById(userId).orElse(null);
        Item item = itemRepository.findById(itemId).orElse(null);
        if (currentUser == null || otherUser == null || item == null) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(messageRepository.findDirectMessages(currentUser, otherUser, item).stream().map(this::convertToDto).toList());
    }

    @GetMapping("/messages/conversations")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getConversations(Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser == null) return ResponseEntity.ok(Collections.emptyList());
        Map<String, Map<String, Object>> conversations = new LinkedHashMap<>();
        for (Message msg : messageRepository.findAllPrivateMessagesForUser(currentUser)) {
            User other = msg.getSender().getUserId().equals(currentUser.getUserId()) ? msg.getReceiver() : msg.getSender();
            Item item = msg.getItem();
            if (other == null || item == null) continue;
            String key = other.getUserId() + "-" + item.getId();
            if (!conversations.containsKey(key)) {
                Map<String, Object> conv = new HashMap<>();
                conv.put("userId", other.getUserId());
                conv.put("username", other.getUsername());
                conv.put("avatarUrl", other.getAvatarUrl());
                conv.put("lastMessage", msg.getContent());
                conv.put("lastMessageAt", msg.getCreatedAt());
                conv.put("lastMessageSenderId", msg.getSender().getUserId());
                conv.put("itemId", item.getId());
                conv.put("itemTitle", item.getTitle());
                conv.put("itemStatus", item.getStatus().toString());
                conversations.put(key, conv);
            }
        }
        return ResponseEntity.ok(new ArrayList<>(conversations.values()));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private Message saveMessage(User sender, User receiver, Item item, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setItem(item);
        message.setContent(content);
        return messageRepository.save(message);
    }

    private MessageDTO convertToDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getUserId());
        dto.setSenderName(message.getSender().getUsername());
        if (message.getReceiver() != null) dto.setReceiverId(message.getReceiver().getUserId());
        if (message.getItem() != null) {
            dto.setItemId(message.getItem().getId());
            dto.setItemTitle(message.getItem().getTitle());
        }
        dto.setContent(message.getContent());
        dto.setSenderWarningMarks(message.getSender().getWarningMarks());
        dto.setSenderAvatar(message.getSender().getAvatarUrl());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
