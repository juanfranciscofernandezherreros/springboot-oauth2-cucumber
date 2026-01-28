package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.dto.UpdateUserRequest;
import com.sixgroup.refit.ejemplo.dto.UserResponse;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.service.UserService;
import com.sixgroup.refit.ejemplo.utils.constants.UserEndpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UserEndpoints.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(UserEndpoints.ME)
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserStatus(userDetails.getUsername());

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping(UserEndpoints.UPDATE)
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequest request
    ) {
        User updatedUser =
                userService.updateMyProfile(userDetails.getUsername(), request);

        UserResponse response = new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );

        return ResponseEntity.ok(response);
    }
}
