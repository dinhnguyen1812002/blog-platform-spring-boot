package com.Nguyen.blogplatform.domain.user.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProfileSecurityIntegrationTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void openApiDocumentIsAvailable() throws Exception {
        mvc.perform(get("/api-docs")).andExpect(status().isOk());
    }

    @Test void anonymousCannotReadOwnOrAdminProfile() throws Exception {
        mvc.perform(get("/api/v1/profile")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/users/missing/profile")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER")
    void normalUserCannotReadAdminProfile() throws Exception {
        mvc.perform(get("/api/v1/admin/users/missing/profile")).andExpect(status().isForbidden());
    }

    @Test void publicProfileIsAccessibleWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/users/profile/nonexistent-profile-contract-test"))
                .andExpect(status().isNotFound());
    }

    @Test void csrfTokenProtectsCookieRequests() throws Exception {
        var result = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        var session = (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false);
        var matcher = java.util.regex.Pattern.compile("\"token\":\"([^\"]+)\"").matcher(result.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertTrue(matcher.find());
        String token = matcher.group(1);
        var cookie = new jakarta.servlet.http.Cookie("JSESSIONID", session.getId());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/profile")
                .session(session).cookie(cookie).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/profile")
                .session(session).cookie(cookie).header("X-CSRF-TOKEN", token)
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER")
    void normalUserCannotUseDebugOrCreateRoles() throws Exception {
        mvc.perform(get("/api/debug/users")).andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/role/add")
                .contentType("application/json").content("{\"name\":\"ROLE_ADMIN\"}")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test void invalidPageAndSearchReturnBadRequest() throws Exception {
        mvc.perform(get("/api/v1/post").param("size", "10000")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/search").param("q", " ")).andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminProfileRouteReachesService() throws Exception {
        mvc.perform(get("/api/v1/admin/users/nonexistent-profile-contract-test/profile"))
                .andExpect(status().isNotFound());
    }
}
