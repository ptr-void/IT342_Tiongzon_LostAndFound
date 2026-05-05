package edu.cit.tiongzon.lostandfound.feature.items;

import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<ItemDTO> items = itemRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable("id") Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(convertToDto(item.get()));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Item not found"));
    }

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemDTO dto) {
        Optional<User> reporter = userRepository.findByUserId(dto.getReporterId());
        if (reporter.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid reporter ID"));
        }

        Item item = new Item();
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setStatus(dto.getStatus() != null ? dto.getStatus() : Item.ItemStatus.LOST);
        item.setCategory(dto.getCategory());
        item.setLocationLat(dto.getLocationLat());
        item.setLocationLng(dto.getLocationLng());
        item.setLocationDescription(dto.getLocationDescription());
        item.setImagePath(dto.getImagePath());
        item.setReporter(reporter.get());

        Item savedItem = itemRepository.save(item);
        return ResponseEntity.ok(convertToDto(savedItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable("id") Long id, @RequestBody ItemDTO dto) {
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Item not found"));
        }

        Item item = itemOpt.get();
        if (dto.getTitle() != null) item.setTitle(dto.getTitle());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getStatus() != null) item.setStatus(dto.getStatus());
        if (dto.getCategory() != null) item.setCategory(dto.getCategory());
        if (dto.getLocationLat() != null) item.setLocationLat(dto.getLocationLat());
        if (dto.getLocationLng() != null) item.setLocationLng(dto.getLocationLng());
        if (dto.getLocationDescription() != null) item.setLocationDescription(dto.getLocationDescription());
        if (dto.getImagePath() != null) item.setImagePath(dto.getImagePath());

        Item updatedItem = itemRepository.save(item);
        return ResponseEntity.ok(convertToDto(updatedItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("message", "Item not found"));
        }
        itemRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
    }

    private ItemDTO convertToDto(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setStatus(item.getStatus());
        dto.setCategory(item.getCategory());
        dto.setLocationLat(item.getLocationLat());
        dto.setLocationLng(item.getLocationLng());
        dto.setLocationDescription(item.getLocationDescription());
        dto.setImagePath(item.getImagePath());
        if (item.getReporter() != null) {
            dto.setReporterId(item.getReporter().getUserId());
            dto.setReporterName(item.getReporter().getUsername());
            dto.setReporterEmail(item.getReporter().getEmail());
            dto.setReporterWarningMarks(item.getReporter().getWarningMarks());
            dto.setReporterAvatar(item.getReporter().getAvatarUrl());
        }
        dto.setCreatedAt(item.getCreatedAt());
        dto.setLastUpdate(item.getLastUpdate());
        return dto;
    }
}
