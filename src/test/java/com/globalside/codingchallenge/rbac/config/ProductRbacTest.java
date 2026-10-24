package com.globalside.codingchallenge.rbac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalside.codingchallenge.rbac.model.ErrorResponse;
import com.globalside.codingchallenge.rbac.model.ProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.globalside.codingchallenge.rbac.config.CustomAccessDeniedHandler.ACCESS_DENIED_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductRbacTest {

    private static final String ADMIN = "admin";
    private static final String USER = "user";
    private static final String PRODUCT_NAME = "Intel CPU";

    private static final String PRODUCT_JSON = """
            {
              "name": "%s",
              "description": "Core2Duo",
              "price": 190.99,
              "currency": "EUR",
              "category": "Electronics",
              "brand": "Intel",
              "color": "Green"
            }
            """.formatted(PRODUCT_NAME);

    private static final ErrorResponse ACCESS_DENIED_RESPONSE =
            new ErrorResponse(ACCESS_DENIED_MESSAGE);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanPerformAllCrudOperations() throws Exception {
        asAdmin(get("/products"))
                .andExpect(status().isOk());

        ProductDto created = createProductAsAdmin();

        asAdmin(put("/products/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON))
                .andExpect(status().isOk());

        asAdmin(delete("/products/" + created.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void userCanReadProducts() throws Exception {
        asUser(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void userCanReadProductById() throws Exception {
        ProductDto created = createProductAsAdmin();

        ProductDto read = readResponse(
                asUser(get("/products/" + created.getId())).andExpect(status().isOk()),
                ProductDto.class);

        assertEquals(created.getId(), read.getId());
        assertEquals(PRODUCT_NAME, read.getName());
    }

    @Test
    void userCannotCreateProduct() throws Exception {
        expectForbidden(asUser(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCT_JSON)));
    }

    @Test
    void userCannotUpdateProduct() throws Exception {
        expectForbidden(asUser(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCT_JSON)));
    }

    @Test
    void userCannotDeleteProduct() throws Exception {
        expectForbidden(asUser(delete("/products/1")));
    }

    private ProductDto createProductAsAdmin() throws Exception {
        return readResponse(
                asAdmin(post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(PRODUCT_JSON))
                        .andExpect(status().isOk()),
                ProductDto.class);
    }

    private ResultActions asAdmin(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(httpBasic(ADMIN, ADMIN)));
    }

    private ResultActions asUser(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(httpBasic(USER, USER)));
    }

    private void expectForbidden(ResultActions actions) throws Exception {
        ErrorResponse error = readResponse(actions.andExpect(status().isForbidden()), ErrorResponse.class);
        assertEquals(ACCESS_DENIED_RESPONSE, error);
    }

    private <T> T readResponse(ResultActions actions, Class<T> type) throws Exception {
        MvcResult result = actions.andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), type);
    }
}
