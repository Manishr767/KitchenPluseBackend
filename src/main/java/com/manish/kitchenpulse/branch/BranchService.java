package com.manish.kitchenpulse.branch;

import java.util.List;
import java.util.Locale;

import com.manish.kitchenpulse.common.BusinessRuleException;
import com.manish.kitchenpulse.common.ConflictException;
import com.manish.kitchenpulse.common.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public API of the branch module. Other modules call this service; they never
 * touch the repository or the table directly.
 */
@Service
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branches;

    BranchService(BranchRepository branches) {
        this.branches = branches;
    }

    @Transactional
    public BranchResponse create(CreateBranchRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (branches.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Branch code '" + code + "' already exists");
        }
        Branch saved = branches.saveAndFlush(
                new Branch(code, request.name().trim(), request.address().trim()));
        return BranchResponse.from(saved);
    }

    public BranchResponse get(Long id) {
        return BranchResponse.from(find(id));
    }

    public List<BranchResponse> list() {
        return branches.findAllByOrderByCodeAsc().stream().map(BranchResponse::from).toList();
    }

    @Transactional
    public BranchResponse update(Long id, UpdateBranchRequest request) {
        Branch branch = find(id);
        branch.setName(request.name().trim());
        branch.setAddress(request.address().trim());
        return BranchResponse.from(branch);
    }

    @Transactional
    public BranchResponse setActive(Long id, boolean active) {
        Branch branch = find(id);
        branch.setActive(active);
        return BranchResponse.from(branch);
    }

    /**
     * For other modules: fails with a business-rule error if the branch does
     * not exist or is inactive.
     */
    public void requireActive(Long id) {
        Branch branch = branches.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Branch " + id + " does not exist"));
        if (!branch.isActive()) {
            throw new BusinessRuleException("Branch " + branch.getCode() + " is inactive");
        }
    }

    private Branch find(Long id) {
        return branches.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch " + id + " not found"));
    }
}
