package edu.cit.tiongzon.lostandfound.feature.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.tiongzon.lostandfound.shared.utils.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private edu.cit.tiongzon.lostandfound.shared.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildUser(String username) {
        User user = new User();
        user.setUserId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        return user;
    }

    // TC-USER-01: GET /users/me valid token
    @Test
    @DisplayName("TC-USER-01: GET /users/me - valid token returns user")
    void testGetMeValid() throws Exception {
        when(jwtUtils.extractUsername("valid-token")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(buildUser("john")));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    // TC-USER-02: GET /users/me missing/invalid header
    @Test
    @DisplayName("TC-USER-02: GET /users/me - missing Authorization header returns 401")
    void testGetMeMissingToken() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "InvalidHeader"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    // TC-USER-03: PATCH /users/me/avatar valid
    @Test
    @DisplayName("TC-USER-03: PATCH /users/me/avatar - valid URL updates avatar")
    void testUpdateAvatarSuccess() throws Exception {
        User user = buildUser("jane");
        when(jwtUtils.extractUsername("valid-token")).thenReturn("jane");
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        Map<String, String> body = Map.of("avatarUrl", "https://example.com/avatar.png");

        mockMvc.perform(patch("/users/me/avatar")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Avatar updated successfully"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"));
    }

    // TC-USER-04: PATCH /users/me/avatar blank URL
    @Test
    @DisplayName("TC-USER-04: PATCH /users/me/avatar - blank avatarUrl returns 400")
    void testUpdateAvatarBlankUrl() throws Exception {
        when(jwtUtils.extractUsername("valid-token")).thenReturn("jane");
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(buildUser("jane")));

        Map<String, String> body = Map.of("avatarUrl", "");

        mockMvc.perform(patch("/users/me/avatar")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("avatarUrl is required"));
    }
}
