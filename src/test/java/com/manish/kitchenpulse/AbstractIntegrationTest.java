package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.testcontainers.containers.PostgreSQLContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Boots the real application against a real PostgreSQL (Testcontainers), so
 * Flyway migrations, Hibernate schema validation, constraints and security are
 * all exercised together. Requires Docker. One container is shared by all tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "Admin-Pass-123";
    protected static final String USER_PASSWORD = "User-Pass-12345";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    /** A user created through the API, with a ready-to-use token. */
    protected record Account(long id, String email, String password, String token) {
    }

    // ---------- request helpers ----------

    protected ResultActions getJson(String url, String token) throws Exception {
        return mvc.perform(withAuth(get(url), token));
    }

    protected ResultActions postJson(String url, String token, Object body) throws Exception {
        MockHttpServletRequestBuilder request = withAuth(post(url), token);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        }
        return mvc.perform(request);
    }

    protected ResultActions putJson(String url, String token, Object body) throws Exception {
        MockHttpServletRequestBuilder request = withAuth(put(url), token);
        request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        return mvc.perform(request);
    }

    private static MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request, String token) {
        return (token == null) ? request : request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected static long idOf(ResultActions result) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    // ---------- fixtures ----------

    protected String login(String email, String password) throws Exception {
        ResultActions result = postJson("/api/auth/login", null,
                Map.of("email", email, "password", password));
        result.andExpect(status().isOk());
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.accessToken");
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected long createBranch() throws Exception {
        Map<String, Object> body = Map.of(
                "code", unique("b").replace("-", "").substring(0, 8),
                "name", "Branch " + unique("n"),
                "address", "1 Test Street");
        ResultActions result = postJson("/api/branches", adminToken(), body);
        result.andExpect(status().isCreated());
        return idOf(result);
    }

    /** Creates a user with the given role (branchId only for BRANCH_STAFF) and logs them in. */
    protected Account createUser(String role, Long branchId) throws Exception {
        String email = unique(role.toLowerCase().replace("_", "")) + "@test.local";
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Test " + role);
        body.put("email", email);
        body.put("password", USER_PASSWORD);
        body.put("role", role);
        if (branchId != null) {
            body.put("branchId", branchId);
        }
        ResultActions result = postJson("/api/users", adminToken(), body);
        result.andExpect(status().isCreated());
        return new Account(idOf(result), email, USER_PASSWORD, login(email, USER_PASSWORD));
    }

    protected Account createBranchStaff(long branchId) throws Exception {
        return createUser("BRANCH_STAFF", branchId);
    }

    protected Account createStaff(String nonBranchRole) throws Exception {
        return createUser(nonBranchRole, null);
    }
}
