package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

class IngredientRequestApiTest extends AbstractIntegrationTest {

    private long unitId(String symbol) throws Exception {
        String body = getJson("/api/units", adminToken()).andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$[?(@.symbol=='" + symbol + "')].id");
        return ids.get(0).longValue();
    }

    private long createIngredient(String name) throws Exception {
        var result = postJson("/api/ingredients", adminToken(),
                Map.of("name", name, "stockUnitId", unitId("kg")));
        result.andExpect(status().isCreated());
        return idOf(result);
    }

    @Test
    void branchStaff_canCreateAndSubmitRequest_andCentralOfficeCanReviewIt() throws Exception {
        long branchId = createBranch();
        long ingredientId = createIngredient(unique("Rice"));
        String branchToken = createBranchStaff(branchId).token();
        String officeToken = createStaff("CENTRAL_OFFICE_STAFF").token();

        var created = postJson("/api/ingredient-requests", branchToken,
                Map.of("items", List.of(Map.of("ingredientId", ingredientId, "quantity", 12.5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.branchId").value(branchId))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.items[0].ingredientId").value(ingredientId))
                .andExpect(jsonPath("$.items[0].requestedQuantity").value(12.5));
        long requestId = idOf(created);

        postJson("/api/ingredient-requests/" + requestId + "/submit", branchToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty());

        getJson("/api/ingredient-requests", officeToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + requestId + ")]").isNotEmpty());
    }

    @Test
    void branchStaff_canOnlySeeTheirOwnRequests() throws Exception {
        long branchA = createBranch();
        long branchB = createBranch();
        long ingredientId = createIngredient(unique("Oil"));
        String tokenA = createBranchStaff(branchA).token();
        String tokenB = createBranchStaff(branchB).token();

        var created = postJson("/api/ingredient-requests", tokenA,
                Map.of("items", List.of(Map.of("ingredientId", ingredientId, "quantity", 2))))
                .andExpect(status().isCreated());
        long requestId = idOf(created);

        getJson("/api/ingredient-requests/" + requestId, tokenB).andExpect(status().isNotFound());
        getJson("/api/ingredient-requests", tokenB)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void centralOfficeCannotCreateRequest_andBranchStaffCannotSubmitAnotherBranchesRequest() throws Exception {
        long branchA = createBranch();
        long branchB = createBranch();
        long ingredientId = createIngredient(unique("Flour"));
        String tokenA = createBranchStaff(branchA).token();
        String tokenB = createBranchStaff(branchB).token();
        String office = createStaff("CENTRAL_OFFICE_STAFF").token();

        postJson("/api/ingredient-requests", office,
                Map.of("items", List.of(Map.of("ingredientId", ingredientId, "quantity", 1))))
                .andExpect(status().isForbidden());

        long requestId = idOf(postJson("/api/ingredient-requests", tokenA,
                Map.of("items", List.of(Map.of("ingredientId", ingredientId, "quantity", 1))))
                .andExpect(status().isCreated()));
        postJson("/api/ingredient-requests/" + requestId + "/submit", tokenB, null)
                .andExpect(status().isNotFound());
    }

    @Test
    void requestRejectsDuplicateIngredientsAndNonPositiveQuantities() throws Exception {
        long branchId = createBranch();
        long ingredientId = createIngredient(unique("Beef"));
        String token = createBranchStaff(branchId).token();

        postJson("/api/ingredient-requests", token, Map.of("items", List.of(
                Map.of("ingredientId", ingredientId, "quantity", 1),
                Map.of("ingredientId", ingredientId, "quantity", 2))))
                .andExpect(status().isUnprocessableEntity());

        postJson("/api/ingredient-requests", token,
                Map.of("items", List.of(Map.of("ingredientId", ingredientId, "quantity", 0))))
                .andExpect(status().isBadRequest());
    }
}
