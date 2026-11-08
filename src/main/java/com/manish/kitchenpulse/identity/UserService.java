package com.manish.kitchenpulse.identity;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.manish.kitchenpulse.branch.BranchService;
import com.manish.kitchenpulse.common.BusinessRuleException;
import com.manish.kitchenpulse.common.ConflictException;
import com.manish.kitchenpulse.common.NotFoundException;
import com.manish.kitchenpulse.common.PageResponse;
import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import com.manish.kitchenpulse.common.security.Role;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    /** BCrypt silently ignores/rejects input beyond 72 bytes. */
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository users;
    private final BranchService branches;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository users, BranchService branches, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.branches = branches;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email '" + email + "' is already registered");
        }
        validateRoleAndBranch(request.role(), request.branchId());
        checkPasswordLength(request.password());

        User user = new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), request.role(), request.branchId());
        return UserResponse.from(users.saveAndFlush(user));
    }

    public PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.from(users.findAll(pageable).map(UserResponse::from));
    }

    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, AuthenticatedUser actor) {
        User user = find(id);
        if (user.getId().equals(actor.id()) && user.getRole() != request.role()) {
            throw new BusinessRuleException("You cannot change your own role");
        }
        validateRoleAndBranch(request.role(), request.branchId());

        user.setName(request.name().trim());
        user.setRole(request.role());
        user.setBranchId(request.branchId());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active, AuthenticatedUser actor) {
        User user = find(id);
        if (!active && user.getId().equals(actor.id())) {
            throw new BusinessRuleException("You cannot deactivate your own account");
        }
        user.setActive(active);
        return UserResponse.from(user);
    }

    @Transactional
    public void changeOwnPassword(AuthenticatedUser actor, ChangePasswordRequest request) {
        User user = find(actor.id());
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        checkPasswordLength(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = find(id);
        checkPasswordLength(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    // ---- rules shared with the database constraint chk_users_branch_role ----

    private void validateRoleAndBranch(Role role, Long branchId) {
        if (role == Role.BRANCH_STAFF) {
            if (branchId == null) {
                throw new BusinessRuleException("Branch staff must be assigned to a branch");
            }
            branches.requireActive(branchId);
        } else if (branchId != null) {
            throw new BusinessRuleException("Only branch staff can be assigned to a branch");
        }
    }

    private void checkPasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new BusinessRuleException(
                    "Password is too long (maximum " + BCRYPT_MAX_BYTES + " bytes)");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private User find(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }
}
