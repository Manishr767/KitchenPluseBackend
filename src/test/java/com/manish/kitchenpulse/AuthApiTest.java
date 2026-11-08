package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AuthApiTest extends AbstractIntegrationTest {

    @Test
    void login_withValidCredentials_returnsTokenAndUserWithoutPasswordHash() throws Exception {
        postJson("/api/auth/login", null, Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void login_ignoresEmailCase() throws Exception {
        postJson("/api/auth/login", null, Map.of("email", "ADMIN@Test.Local", "password", ADMIN_PASSWORD))
                .andExpect(status().isOk());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        postJson("/api/auth/login", null, Map.of("email", ADMIN_EMAIL, "password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void login_withUnknownEmail_returnsSameErrorAsWrongPassword() throws Exception {
        postJson("/api/auth/login", null, Map.of("email", "nobody@test.local", "password", "whatever123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void login_withBlankFields_returns400() throws Exception {
        postJson("/api/auth/login", null, Map.of("email", "", "password", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void login_whenUserIsDeactivated_returns401() throws Exception {
        Account staff = createStaff("CENTRAL_OFFICE_STAFF");
        postJson("/api/users/" + staff.id() + "/deactivate", adminToken(), null)
                .andExpect(status().isOk());

        postJson("/api/auth/login", null, Map.of("email", staff.email(), "password", staff.password()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void existingToken_stopsWorking_whenUserIsDeactivated() throws Exception {
        Account staff = createStaff("CENTRAL_OFFICE_STAFF");
        getJson("/api/users/me", staff.token()).andExpect(status().isOk());

        postJson("/api/users/" + staff.id() + "/deactivate", adminToken(), null)
                .andExpect(status().isOk());

        getJson("/api/users/me", staff.token()).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        getJson("/api/ingredients", null).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns401() throws Exception {
        getJson("/api/ingredients", "not.a.token").andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsTheLoggedInUser() throws Exception {
        Account staff = createStaff("WAREHOUSE_MANAGER");

        getJson("/api/users/me", staff.token())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(staff.email()))
                .andExpect(jsonPath("$.role").value("WAREHOUSE_MANAGER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void changeOwnPassword_thenOldPasswordNoLongerWorks() throws Exception {
        Account staff = createStaff("CENTRAL_OFFICE_STAFF");

        postJson("/api/users/me/password", staff.token(),
                Map.of("currentPassword", staff.password(), "newPassword", "Brand-New-Pass-1"))
                .andExpect(status().isNoContent());

        postJson("/api/auth/login", null, Map.of("email", staff.email(), "password", staff.password()))
                .andExpect(status().isUnauthorized());
        postJson("/api/auth/login", null, Map.of("email", staff.email(), "password", "Brand-New-Pass-1"))
                .andExpect(status().isOk());
    }

    @Test
    void changeOwnPassword_withWrongCurrentPassword_returns422() throws Exception {
        Account staff = createStaff("CENTRAL_OFFICE_STAFF");

        postJson("/api/users/me/password", staff.token(),
                Map.of("currentPassword", "not-my-password", "newPassword", "Brand-New-Pass-1"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void healthAndApiDocs_arePublic() throws Exception {
        getJson("/actuator/health", null).andExpect(status().isOk());
        getJson("/v3/api-docs", null).andExpect(status().isOk());
    }
}
