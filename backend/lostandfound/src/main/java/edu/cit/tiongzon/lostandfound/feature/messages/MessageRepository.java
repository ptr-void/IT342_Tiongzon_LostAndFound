package edu.cit.tiongzon.lostandfound.feature.messages;

import edu.cit.tiongzon.lostandfound.feature.items.Item;
import edu.cit.tiongzon.lostandfound.feature.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.receiver IS NULL ORDER BY m.createdAt ASC")
    List<Message> findByReceiverIsNullOrderByCreatedAtAsc();

    @Query("SELECT m FROM Message m JOIN FETCH m.sender LEFT JOIN FETCH m.receiver LEFT JOIN FETCH m.item WHERE m.item = :item AND ((m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)) ORDER BY m.createdAt ASC")
    List<Message> findDirectMessages(@Param("user1") User user1, @Param("user2") User user2, @Param("item") Item item);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender LEFT JOIN FETCH m.receiver LEFT JOIN FETCH m.item WHERE m.receiver IS NOT NULL AND m.item IS NOT NULL AND (m.sender = :user OR m.receiver = :user) ORDER BY m.createdAt DESC")
    List<Message> findAllPrivateMessagesForUser(@Param("user") User user);
}
