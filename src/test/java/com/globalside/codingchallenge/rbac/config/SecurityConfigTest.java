package com.globalside.codingchallenge.rbac.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAuthenticate() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    @Test
    void userCanAuthenticate() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("user", "user")))
                .andExpect(status().isOk());
    }

    @Test
    void invalidCredentialsReturn401() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserNeitherAdminNorUserReturns401() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("stranger", "password")))
                .andExpect(status().isUnauthorized());
    }
}
