package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;

class BranchApiTest extends AbstractIntegrationTest {

    @Test
    void admin_canCreateBranch_andCodeIsStoredUpperCase() throws Exception {
        String code = unique("gn").replace("-", "").substring(0, 8).toLowerCase();

        postJson("/api/branches", adminToken(),
                Map.of("code", code, "name", "Gangnam", "address", "1 Teheran-ro"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code.toUpperCase()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createBranch_withDuplicateCodeInDifferentCase_returns409() throws Exception {
        String code = unique("dup").replace("-", "").substring(0, 8);
        Map<String, Object> body = Map.of("code", code, "name", "First", "address", "A");
        postJson("/api/branches", adminToken(), body).andExpect(status().isCreated());

        postJson("/api/branches", adminToken(),
                Map.of("code", code.toLowerCase(), "name", "Second", "address", "B"))
                .andExpect(status().isConflict());
    }

    @Test
    void createBranch_withBlankName_returns400_andNamesTheField() throws Exception {
        postJson("/api/branches", adminToken(),
                Map.of("code", unique("x").replace("-", ""), "name", " ", "address", "A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createBranch_withInvalidCodeCharacters_returns400() throws Exception {
        postJson("/api/branches", adminToken(),
                Map.of("code", "bad code!", "name", "N", "address", "A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists());
    }

    @Test
    void nonAdminRoles_cannotCreateBranch_return403() throws Exception {
        long branchId = createBranch();
        String branchStaff = createBranchStaff(branchId).token();
        String office = createStaff("CENTRAL_OFFICE_STAFF").token();
        Map<String, Object> body = Map.of("code", unique("n").replace("-", ""), "name", "N", "address", "A");

        postJson("/api/branches", branchStaff, body).andExpect(status().isForbidden());
        postJson("/api/branches", office, body).andExpect(status().isForbidden());
    }

    @Test
    void branchStaff_canReadOwnBranch_butNotAnotherOne() throws Exception {
        long own = createBranch();
        long other = createBranch();
        String token = createBranchStaff(own).token();

        getJson("/api/branches/" + own, token).andExpect(status().isOk());
        getJson("/api/branches/" + other, token).andExpect(status().isForbidden());
        getJson("/api/branches", token).andExpect(status().isForbidden());
    }

    @Test
    void deliveryPerson_cannotReadBranches() throws Exception {
        long branchId = createBranch();
        String token = createStaff("DELIVERY_PERSON").token();

        getJson("/api/branches/" + branchId, token).andExpect(status().isForbidden());
        getJson("/api/branches", token).andExpect(status().isForbidden());
    }

    @Test
    void centralOffice_canListAndReadAnyBranch() throws Exception {
        long branchId = createBranch();
        String token = createStaff("CENTRAL_OFFICE_STAFF").token();

        getJson("/api/branches", token).andExpect(status().isOk());
        getJson("/api/branches/" + branchId, token).andExpect(status().isOk());
    }

    @Test
    void admin_canUpdateBranch_andCodeStaysTheSame() throws Exception {
        long id = createBranch();
        String before = getJson("/api/branches/" + id, adminToken()).andReturn().getResponse().getContentAsString();
        String code = com.jayway.jsonpath.JsonPath.read(before, "$.code");

        putJson("/api/branches/" + id, adminToken(), Map.of("name", "Renamed", "address", "New address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.address").value("New address"))
                .andExpect(jsonPath("$.code").value(code));
    }

    @Test
    void getUnknownBranch_returns404() throws Exception {
        getJson("/api/branches/999999999", adminToken()).andExpect(status().isNotFound());
    }

    @Test
    void inactiveBranch_cannotReceiveNewStaff() throws Exception {
        long id = createBranch();
        postJson("/api/branches/" + id + "/deactivate", adminToken(), null).andExpect(status().isOk());

        postJson("/api/users", adminToken(), Map.of(
                "name", "Late Joiner",
                "email", unique("late") + "@test.local",
                "password", USER_PASSWORD,
                "role", "BRANCH_STAFF",
                "branchId", id))
                .andExpect(status().isUnprocessableEntity());
    }
}
