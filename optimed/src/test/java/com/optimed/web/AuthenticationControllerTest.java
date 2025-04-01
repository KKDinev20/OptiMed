package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.dto.RegisterRequest;
import com.optimed.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("testuser@example.com");
    }

    @Test
    void shouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void shouldReturnLoginPageWithError() throws Exception {
        mockMvc.perform(get("/auth/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "Invalid username or password"));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldReturnRegisterPage() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        when(userService.findByUsername(registerRequest.getUsername())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/auth/register")
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        verify(userService, times(1)).registerUser(registerRequest);
    }

    @Test
    void shouldReturnErrorWhenUsernameAlreadyExists() throws Exception {
        when(userService.findByUsername(registerRequest.getUsername())).thenReturn(java.util.Optional.of(new com.optimed.entity.User()));

        mockMvc.perform(post("/auth/register")
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void shouldReturnErrorWhenRegistrationHasValidationErrors() throws Exception {
        registerRequest.setUsername("");

        mockMvc.perform(post("/auth/register")
                        .flashAttr("registerRequest", registerRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username"));
    }
}
