package edu.cit.tiongzon.lostandfound.feature.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import edu.cit.tiongzon.lostandfound.shared.utils.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private edu.cit.tiongzon.lostandfound.shared.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildReporter() {
        User reporter = new User();
        reporter.setUserId(1L);
        reporter.setUsername("reporter1");
        reporter.setEmail("reporter@test.com");
        return reporter;
    }

    private Item buildSampleItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setTitle("Lost Wallet");
        item.setDescription("Brown leather wallet");
        item.setStatus(Item.ItemStatus.LOST);
        item.setCategory(Item.ItemCategory.VALUABLES);
        item.setReporter(buildReporter());
        return item;
    }

    
    @Test
    @DisplayName("TC-ITEM-01: GET /items - returns all items")
    void testGetAllItems() throws Exception {
        when(itemRepository.findAll()).thenReturn(List.of(buildSampleItem(1L)));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Lost Wallet"));
    }

    
    @Test
    @DisplayName("TC-ITEM-02: GET /items/{id} - valid id returns item")
    void testGetItemByIdFound() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(buildSampleItem(1L)));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lost Wallet"))
                .andExpect(jsonPath("$.status").value("LOST"));
    }

    
    @Test
    @DisplayName("TC-ITEM-03: GET /items/{id} - not found returns 404")
    void testGetItemByIdNotFound() throws Exception {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

    
    @Test
    @DisplayName("TC-ITEM-04: POST /items - valid payload creates item")
    void testCreateItemSuccess() throws Exception {
        User reporter = buildReporter();
        Item saved = buildSampleItem(10L);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(reporter));
        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemDTO dto = new ItemDTO();
        dto.setTitle("Lost Wallet");
        dto.setDescription("Brown leather wallet");
        dto.setStatus(Item.ItemStatus.LOST);
        dto.setCategory(Item.ItemCategory.VALUABLES);
        dto.setReporterId(1L);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lost Wallet"));
    }

    
    @Test
    @DisplayName("TC-ITEM-05: POST /items - invalid reporterId returns 400")
    void testCreateItemInvalidReporter() throws Exception {
        when(userRepository.findByUserId(999L)).thenReturn(Optional.empty());

        ItemDTO dto = new ItemDTO();
        dto.setTitle("Lost Key");
        dto.setStatus(Item.ItemStatus.LOST);
        dto.setCategory(Item.ItemCategory.OTHER);
        dto.setReporterId(999L);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid reporter ID"));
    }

    
    @Test
    @DisplayName("TC-ITEM-06: PUT /items/{id} - updates item successfully")
    void testUpdateItem() throws Exception {
        Item existing = buildSampleItem(1L);
        Item updated = buildSampleItem(1L);
        updated.setStatus(Item.ItemStatus.FOUND);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenReturn(updated);

        ItemDTO dto = new ItemDTO();
        dto.setStatus(Item.ItemStatus.FOUND);

        mockMvc.perform(put("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOUND"));
    }

    
    @Test
    @DisplayName("TC-ITEM-07: DELETE /items/{id} - deletes item successfully")
    void testDeleteItem() throws Exception {
        when(itemRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item deleted successfully"));
    }
}
