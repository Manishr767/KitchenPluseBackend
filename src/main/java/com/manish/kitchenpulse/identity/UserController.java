package com.manish.kitchenpulse.identity;

import com.manish.kitchenpulse.common.PageResponse;
import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/users")
@Tag(name = "Users")
@PreAuthorize("hasRole('ADMIN')") // default for this controller; "me" endpoints override it
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.get(actor.id());
    }

    @PostMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void changeOwnPassword(@AuthenticationPrincipal AuthenticatedUser actor,
                                  @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeOwnPassword(actor, request);
    }

    @GetMapping
    public PageResponse<UserResponse> list(
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return userService.list(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.update(id, request, actor);
    }

    @PostMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.setActive(id, true, actor);
    }

    @PostMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.setActive(id, false, actor);
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
    }
}
