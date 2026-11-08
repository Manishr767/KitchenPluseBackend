package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

class UserApiTest extends AbstractIntegrationTest {

    private Map<String, Object> userBody(String email, String role, Long branchId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Some User");
        body.put("email", email);
        body.put("password", USER_PASSWORD);
        body.put("role", role);
        if (branchId != null) {
            body.put("branchId", branchId);
        }
        return body;
    }

    @Test
    void admin_canCreateBranchStaff_andResponseNeverContainsPasswordData() throws Exception {
        long branchId = createBranch();

        postJson("/api/users", adminToken(), userBody(unique("bs") + "@test.local", "BRANCH_STAFF", branchId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("BRANCH_STAFF"))
                .andExpect(jsonPath("$.branchId").value(branchId))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void createBranchStaff_withoutBranch_returns422() throws Exception {
        postJson("/api/users", adminToken(), userBody(unique("nb") + "@test.local", "BRANCH_STAFF", null))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createCentralOfficeUser_withBranch_returns422() throws Exception {
        long branchId = createBranch();

        postJson("/api/users", adminToken(),
                userBody(unique("co") + "@test.local", "CENTRAL_OFFICE_STAFF", branchId))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createBranchStaff_forUnknownBranch_returns422() throws Exception {
        postJson("/api/users", adminToken(),
                userBody(unique("ub") + "@test.local", "BRANCH_STAFF", 999999999L))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createUser_withDuplicateEmailInDifferentCase_returns409() throws Exception {
        String email = unique("dupe") + "@test.local";
        postJson("/api/users", adminToken(), userBody(email, "CENTRAL_OFFICE_STAFF", null))
                .andExpect(status().isCreated());

        postJson("/api/users", adminToken(), userBody(email.toUpperCase(), "CENTRAL_OFFICE_STAFF", null))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_withShortPassword_returns400() throws Exception {
        Map<String, Object> body = userBody(unique("sp") + "@test.local", "CENTRAL_OFFICE_STAFF", null);
        body.put("password", "short");

        postJson("/api/users", adminToken(), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createUser_withInvalidEmail_returns400() throws Exception {
        postJson("/api/users", adminToken(), userBody("not-an-email", "CENTRAL_OFFICE_STAFF", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void nonAdminRoles_cannotCreateOrListUsers_return403() throws Exception {
        String office = createStaff("CENTRAL_OFFICE_STAFF").token();
        String warehouse = createStaff("WAREHOUSE_MANAGER").token();

        postJson("/api/users", office, userBody(unique("x") + "@test.local", "CENTRAL_OFFICE_STAFF", null))
                .andExpect(status().isForbidden());
        getJson("/api/users", warehouse).andExpect(status().isForbidden());
    }

    @Test
    void admin_cannotDeactivateOwnAccount_returns422() throws Exception {
        String token = adminToken();
        long myId = idOf(getJson("/api/users/me", token));

        postJson("/api/users/" + myId + "/deactivate", token, null)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void admin_cannotChangeOwnRole_returns422() throws Exception {
        String token = adminToken();
        long myId = idOf(getJson("/api/users/me", token));

        putJson("/api/users/" + myId, token, Map.of("name", "System Admin", "role", "CENTRAL_OFFICE_STAFF"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void admin_canMoveBranchStaffToAnotherBranch() throws Exception {
        long from = createBranch();
        long to = createBranch();
        Account staff = createBranchStaff(from);

        putJson("/api/users/" + staff.id(), adminToken(),
                Map.of("name", "Moved Person", "role", "BRANCH_STAFF", "branchId", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(to));

        // The old token keeps working but now reflects the new branch immediately.
        getJson("/api/branches/" + to, staff.token()).andExpect(status().isOk());
        getJson("/api/branches/" + from, staff.token()).andExpect(status().isForbidden());
    }

    @Test
    void admin_canResetPassword_andUserCanLogInWithTheNewOne() throws Exception {
        Account staff = createStaff("CENTRAL_OFFICE_STAFF");

        putJson("/api/users/" + staff.id() + "/password", adminToken(), Map.of("newPassword", "Reset-Pass-9999"))
                .andExpect(status().isNoContent());

        postJson("/api/auth/login", null, Map.of("email", staff.email(), "password", "Reset-Pass-9999"))
                .andExpect(status().isOk());
    }

    @Test
    void listUsers_isPaged() throws Exception {
        createStaff("CENTRAL_OFFICE_STAFF");

        String body = getJson("/api/users?size=1", adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andReturn().getResponse().getContentAsString();

        Number total = JsonPath.read(body, "$.totalElements");
        org.assertj.core.api.Assertions.assertThat(total.longValue()).isGreaterThanOrEqualTo(2L);
    }
}
