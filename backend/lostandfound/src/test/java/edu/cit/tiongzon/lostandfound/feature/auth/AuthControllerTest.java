package edu.cit.tiongzon.lostandfound.feature.auth;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private edu.cit.tiongzon.lostandfound.shared.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    
    @Test
    @DisplayName("TC-AUTH-01: POST /auth/register - success")
    void testRegisterSuccess() throws Exception {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");

        
        Map<String, String> body = Map.of("username", "newuser", "email", "new@example.com", "password", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration success!"));
    }

    
    @Test
    @DisplayName("TC-AUTH-02: POST /auth/register - duplicate username returns 400")
    void testRegisterDuplicateUsername() throws Exception {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        Map<String, String> body = Map.of("username", "existinguser", "email", "unique@example.com", "password", "pass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists!"));
    }

    
    @Test
    @DisplayName("TC-AUTH-03: POST /auth/register - duplicate email returns 400")
    void testRegisterDuplicateEmail() throws Exception {
        when(userRepository.existsByUsername("brandnewuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        Map<String, String> body = Map.of("username", "brandnewuser", "email", "taken@example.com", "password", "pass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists!"));
    }

    
    @Test
    @DisplayName("TC-AUTH-04: POST /auth/login - valid credentials returns token")
    void testLoginSuccess() throws Exception {
        User stored = new User();
        stored.setUsername("testuser");
        stored.setPassword("$2a$hashed");
        stored.setBanned(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(stored));
        
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);
        when(jwtUtils.generateToken(anyString())).thenReturn("mocked-jwt-token");

        String rawJson = "{\"username\":\"testuser\",\"password\":\"rawpass\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    
    @Test
    @DisplayName("TC-AUTH-05: POST /auth/login - wrong password returns 400")
    void testLoginWrongPassword() throws Exception {
        User stored = new User();
        stored.setUsername("testuser");
        stored.setPassword("$2a$hashed");
        stored.setBanned(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches(any(), anyString())).thenReturn(false);

        String rawJson = "{\"username\":\"testuser\",\"password\":\"wrongpass\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials!"));
    }

    
    @Test
    @DisplayName("TC-AUTH-06: POST /auth/login - banned user returns 403")
    void testLoginBannedUser() throws Exception {
        User stored = new User();
        stored.setUsername("banned");
        stored.setPassword("$2a$hashed");
        stored.setBanned(true);

        when(userRepository.findByUsername("banned")).thenReturn(Optional.of(stored));

        String rawJson = "{\"username\":\"banned\",\"password\":\"anypassword\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Your account has been banned."));
    }
}
