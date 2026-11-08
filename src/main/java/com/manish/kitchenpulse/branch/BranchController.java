package com.manish.kitchenpulse.branch;

import java.util.List;

import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/branches")
@Tag(name = "Branches")
public class BranchController {

    private final BranchService branchService;

    BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CENTRAL_OFFICE_STAFF','WAREHOUSE_MANAGER')")
    public List<BranchResponse> list() {
        return branchService.list();
    }

    /** Branch staff may only read their own branch. Delivery people have no access. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CENTRAL_OFFICE_STAFF','WAREHOUSE_MANAGER')"
            + " or (hasRole('BRANCH_STAFF') and #user.branchId() == #id)")
    public BranchResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return branchService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse create(@Valid @RequestBody CreateBranchRequest request) {
        return branchService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse update(@PathVariable Long id, @Valid @RequestBody UpdateBranchRequest request) {
        return branchService.update(id, request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse activate(@PathVariable Long id) {
        return branchService.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse deactivate(@PathVariable Long id) {
        return branchService.setActive(id, false);
    }
}
