package com.manish.kitchenpulse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WarehouseApiTest extends AbstractIntegrationTest {
 @Test
 void warehouseCanDispatchApprovedQuantitiesAndCannotExceedThemOrDispatchTwice() throws Exception {
  long branch=createBranch();
  var unitJson=getJson("/api/units",adminToken()).andReturn().getResponse().getContentAsString();
  long unitId=((java.util.List<Integer>) com.jayway.jsonpath.JsonPath.read(unitJson,"$[?(@.symbol=='kg')].id")).get(0).longValue();
  long ingredient=idOf(postJson("/api/ingredients",adminToken(),Map.of("name",unique("Rice"),"stockUnitId",unitId)).andExpect(status().isCreated()));
  String branchToken=createBranchStaff(branch).token();
  String officeToken=createStaff("CENTRAL_OFFICE_STAFF").token();
  String warehouseToken=createStaff("WAREHOUSE_MANAGER").token();
  long request=idOf(postJson("/api/ingredient-requests",branchToken,Map.of("items",List.of(Map.of("ingredientId",ingredient,"quantity",10)))).andExpect(status().isCreated()));
  postJson("/api/ingredient-requests/"+request+"/submit",branchToken,null).andExpect(status().isOk());
  postJson("/api/ingredient-requests/"+request+"/review",officeToken,Map.of("items",List.of(Map.of("ingredientId",ingredient,"approvedQuantity",8)))).andExpect(status().isOk());
  getJson("/api/warehouse/approved-requests",warehouseToken).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=="+request+")]").isNotEmpty());
  postJson("/api/warehouse/requests/"+request+"/dispatch",warehouseToken,Map.of("items",List.of(Map.of("ingredientId",ingredient,"dispatchedQuantity",9)))).andExpect(status().isUnprocessableEntity());
  postJson("/api/warehouse/requests/"+request+"/dispatch",branchToken,Map.of("items",List.of(Map.of("ingredientId",ingredient,"dispatchedQuantity",8)))).andExpect(status().isForbidden());
  postJson("/api/warehouse/requests/"+request+"/dispatch",warehouseToken,Map.of("items",List.of(Map.of("ingredientId",ingredient,"dispatchedQuantity",7)),"note","Packed for delivery"))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.ingredientRequestId").value(request)).andExpect(jsonPath("$.items[0].approvedQuantity").value(8)).andExpect(jsonPath("$.items[0].dispatchedQuantity").value(7));
  postJson("/api/warehouse/requests/"+request+"/dispatch",warehouseToken,Map.of("items",List.of(Map.of("ingredientId",ingredient,"dispatchedQuantity",7)))).andExpect(status().isUnprocessableEntity());
  getJson("/api/warehouse/approved-requests",warehouseToken).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=="+request+")]").isEmpty());
  getJson("/api/warehouse/dispatches",warehouseToken).andExpect(status().isOk()).andExpect(jsonPath("$[0].note").value("Packed for delivery"));
 }
}
