package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

class CatalogApiTest extends AbstractIntegrationTest {

    private long unitId(String symbol) throws Exception {
        String body = getJson("/api/units", adminToken()).andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$[?(@.symbol=='" + symbol + "')].id");
        return ids.get(0).longValue();
    }

    private long createIngredient(String name, String unitSymbol) throws Exception {
        var result = postJson("/api/ingredients", adminToken(),
                Map.of("name", name, "stockUnitId", unitId(unitSymbol)));
        result.andExpect(status().isCreated());
        return idOf(result);
    }

    @Test
    void seededUnits_areVisibleToEveryRole() throws Exception {
        String branchStaff = createBranchStaff(createBranch()).token();

        getJson("/api/units", branchStaff)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.symbol=='kg')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.symbol=='box')]").isNotEmpty());
    }

    @Test
    void admin_canCreateCustomUnit_butDuplicateSymbolIsRejected() throws Exception {
        String suffix = unique("u").replace("-", "").substring(0, 6);
        Map<String, Object> body = Map.of("name", "Crate " + suffix, "symbol", "cr" + suffix);

        postJson("/api/units", adminToken(), body).andExpect(status().isCreated());
        postJson("/api/units", adminToken(), Map.of("name", "Other " + suffix, "symbol", "CR" + suffix))
                .andExpect(status().isConflict());
    }

    @Test
    void admin_canCreateIngredient_withItsStockUnit() throws Exception {
        String name = unique("Chicken");

        postJson("/api/ingredients", adminToken(), Map.of("name", name, "stockUnitId", unitId("kg")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.stockUnit.symbol").value("kg"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createIngredient_withDuplicateNameInDifferentCase_returns409() throws Exception {
        String name = unique("Rice");
        createIngredient(name, "kg");

        postJson("/api/ingredients", adminToken(), Map.of("name", name.toUpperCase(), "stockUnitId", unitId("kg")))
                .andExpect(status().isConflict());
    }

    @Test
    void createIngredient_withUnknownUnit_returns422() throws Exception {
        postJson("/api/ingredients", adminToken(), Map.of("name", unique("Ghost"), "stockUnitId", 999999999L))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createIngredient_withoutUnit_returns400() throws Exception {
        postJson("/api/ingredients", adminToken(), Map.of("name", unique("NoUnit")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.stockUnitId").exists());
    }

    @Test
    void branchStaff_canReadIngredients_butCannotCreateOrChangeThem() throws Exception {
        long ingredientId = createIngredient(unique("Onion"), "kg");
        String token = createBranchStaff(createBranch()).token();

        getJson("/api/ingredients", token).andExpect(status().isOk());
        getJson("/api/ingredients/" + ingredientId, token).andExpect(status().isOk());

        postJson("/api/ingredients", token, Map.of("name", unique("Nope"), "stockUnitId", unitId("kg")))
                .andExpect(status().isForbidden());
        putJson("/api/ingredients/" + ingredientId, token, Map.of("name", "Hacked"))
                .andExpect(status().isForbidden());
        postJson("/api/ingredients/" + ingredientId + "/deactivate", token, null)
                .andExpect(status().isForbidden());
    }

    @Test
    void renameIngredient_keepsItsStockUnit() throws Exception {
        long id = createIngredient(unique("Oil"), "l");
        String newName = unique("Sunflower Oil");

        putJson("/api/ingredients/" + id, adminToken(), Map.of("name", newName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.stockUnit.symbol").value("l"));
    }

    @Test
    void renameIngredient_toAnExistingName_returns409() throws Exception {
        String taken = unique("Garlic");
        createIngredient(taken, "kg");
        long other = createIngredient(unique("Ginger"), "kg");

        putJson("/api/ingredients/" + other, adminToken(), Map.of("name", taken.toLowerCase()))
                .andExpect(status().isConflict());
    }

    @Test
    void renameIngredient_toItsOwnNameInDifferentCase_isAllowed() throws Exception {
        String name = unique("Pepper");
        long id = createIngredient(name, "g");

        putJson("/api/ingredients/" + id, adminToken(), Map.of("name", name.toUpperCase()))
                .andExpect(status().isOk());
    }

    @Test
    void deactivatedIngredient_isFilteredByTheActiveParameter() throws Exception {
        String name = unique("Saffron");
        long id = createIngredient(name, "g");
        postJson("/api/ingredients/" + id + "/deactivate", adminToken(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        getJson("/api/ingredients?active=true&q=" + name, adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        getJson("/api/ingredients?active=false&q=" + name, adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByName_isCaseInsensitiveAndPartial() throws Exception {
        String token = unique("zq").replace("-", "");
        createIngredient("Chicken Breast " + token, "kg");
        createIngredient("Chicken Thigh " + token, "kg");

        getJson("/api/ingredients?q=" + token.toUpperCase(), adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
        getJson("/api/ingredients?q=breast " + token, adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].stockUnit.symbol").value("kg"));
    }

    @Test
    void getUnknownIngredient_returns404() throws Exception {
        getJson("/api/ingredients/999999999", adminToken()).andExpect(status().isNotFound());
    }
}
