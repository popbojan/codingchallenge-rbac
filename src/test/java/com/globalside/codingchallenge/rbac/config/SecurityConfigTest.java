package com.globalside.codingchallenge.rbac.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String PRODUCT_JSON = """
            {
              "name": "Intel CPU",
              "description": "Core2Duo",
              "price": 190.99,
              "currency": "EUR",
              "category": "Electronics",
              "brand": "Intel",
              "color": "Green"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("writeRequests")
    void unauthenticatedWriteRequestReturns401(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
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
    void invalidAdminCredentialsReturn401() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidUserCredentialsReturn401() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("writeRequests")
    void wrongPasswordOnWriteRequestReturns401(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request.with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserNeitherAdminNorUserReturns401() throws Exception {
        mockMvc.perform(get("/products")
                        .with(httpBasic("stranger", "password")))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("writeRequests")
    void unknownUserOnWriteRequestReturns401(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request.with(httpBasic("stranger", "password")))
                .andExpect(status().isUnauthorized());
    }

    private static Stream<MockHttpServletRequestBuilder> writeRequests() {
        return Stream.of(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON),
                put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON),
                delete("/products/1")
        );
    }
}
