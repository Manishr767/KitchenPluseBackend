package com.manish.kitchenpulse.warehouse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import com.manish.kitchenpulse.common.BusinessRuleException;
import com.manish.kitchenpulse.common.NotFoundException;
import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import com.manish.kitchenpulse.common.security.Role;
import com.manish.kitchenpulse.request.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class WarehouseService {
 private final IngredientRequestRepository requests; private final IngredientRequestItemRepository requestItems;
 private final WarehouseDispatchRepository dispatches; private final WarehouseDispatchItemRepository dispatchItems;
 WarehouseService(IngredientRequestRepository r,IngredientRequestItemRepository ri,WarehouseDispatchRepository d,WarehouseDispatchItemRepository di){requests=r;requestItems=ri;dispatches=d;dispatchItems=di;}
 public List<IngredientRequestResponse> approvedRequests(AuthenticatedUser user){
  requireRole(user);
  return requests.findAllByStatusInOrderByReviewedAtAsc(List.of("APPROVED","PARTIALLY_APPROVED")).stream()
   .filter(r->dispatches.findByIngredientRequestId(r.getId()).isEmpty())
   .map(r->new IngredientRequestResponse(r.getId(),r.getBranchId(),r.getCreatedByUserId(),r.getStatus(),r.getCreatedAt(),r.getSubmittedAt(),r.getReviewedByUserId(),r.getReviewedAt(),r.getReviewNote(),requestItems.findAllByIngredientRequestIdOrderByIdAsc(r.getId()).stream().map(IngredientRequestItemResponse::from).toList())).toList();
 }
 public List<WarehouseDispatchResponse> history(AuthenticatedUser user){requireRole(user);return dispatches.findAllByOrderByDispatchedAtDesc().stream().map(this::response).toList();}
 @Transactional public WarehouseDispatchResponse dispatch(Long id,CreateWarehouseDispatch body,AuthenticatedUser user){
  requireRole(user);
  IngredientRequest request=requests.findById(id).orElseThrow(()->new NotFoundException("Ingredient request "+id+" not found"));
  if(!List.of("APPROVED","PARTIALLY_APPROVED").contains(request.getStatus()))throw new BusinessRuleException("Only approved requests can be dispatched");
  if(dispatches.findByIngredientRequestId(id).isPresent())throw new BusinessRuleException("This request has already been dispatched");
  var originals=requestItems.findAllByIngredientRequestIdOrderByIdAsc(id);
  var byId=originals.stream().collect(Collectors.toMap(IngredientRequestItem::getIngredientId,x->x));
  long expected=originals.stream().filter(x->x.getApprovedQuantity()!=null&&x.getApprovedQuantity().signum()>0).count();
  if(body.items().size()!=expected)throw new BusinessRuleException("Dispatch must include every ingredient with an approved quantity");
  Set<Long> seen=new HashSet<>(); List<WarehouseDispatchItem> lines=new ArrayList<>();
  for(var line:body.items()){
   if(!seen.add(line.ingredientId()))throw new BusinessRuleException("An ingredient can only appear once");
   var original=byId.get(line.ingredientId());
   if(original==null||original.getApprovedQuantity()==null||original.getApprovedQuantity().signum()<=0)throw new BusinessRuleException("Ingredient "+line.ingredientId()+" has no approved quantity");
   if(line.dispatchedQuantity().compareTo(original.getApprovedQuantity())>0)throw new BusinessRuleException("Dispatched quantity cannot exceed approved quantity");
   lines.add(new WarehouseDispatchItem(null,line.ingredientId(),original.getApprovedQuantity(),line.dispatchedQuantity()));
  }
  var saved=dispatches.saveAndFlush(new WarehouseDispatch(id,user.id(),LocalDateTime.now(ZoneOffset.UTC),body.note()==null||body.note().isBlank()?null:body.note().trim()));
  dispatchItems.saveAll(lines.stream().map(x->new WarehouseDispatchItem(saved.getId(),x.getIngredientId(),x.getApprovedQuantity(),x.getDispatchedQuantity())).toList());
  return response(saved);
 }
 private WarehouseDispatchResponse response(WarehouseDispatch d){var lines=dispatchItems.findAllByWarehouseDispatchIdOrderByIdAsc(d.getId()).stream().map(x->new WarehouseDispatchResponse.Item(x.getId(),x.getIngredientId(),x.getApprovedQuantity(),x.getDispatchedQuantity())).toList();return new WarehouseDispatchResponse(d.getId(),d.getIngredientRequestId(),d.getPreparedByUserId(),d.getDispatchedAt(),d.getNote(),lines);}
 private void requireRole(AuthenticatedUser u){if(u.role()!=Role.WAREHOUSE_MANAGER&&u.role()!=Role.ADMIN)throw new AccessDeniedException("Only warehouse managers can access warehouse operations");}
}
