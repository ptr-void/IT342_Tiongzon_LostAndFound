package edu.cit.tiongzon.lostandfound.feature.items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByReporter_UserId(Long userId);
    List<Item> findByStatus(Item.ItemStatus status);
}
