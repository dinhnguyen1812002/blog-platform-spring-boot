package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.exception.TokenExpiredException;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.service.UserServices;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
@RequestMapping("/api/user")
@RestController
public class UserController {
    @Autowired
    private UserServices userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            userService.sendResetEmail(email);
            model.addAttribute("message", "Reset password email sent");
        } catch (UsernameNotFoundException | MessagingException e) {
            model.addAttribute("error", "Error sending email: " + e.getMessage());
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword, Model model) {
        try {
            userService.resetPassword(token, newPassword);
            model.addAttribute("message", "Password has been reset");
        } catch (TokenExpiredException e) {
            model.addAttribute("error", "Token is invalid or expired");
        }
        return "reset-password";
    }
// this for mvc with thymeleaf but i use rest api for this feature
//    @GetMapping("/update-password")
//    public String showUpdatePasswordForm() {
//        return "update-password";
//    }
//
//    @PostMapping("/update-password")
//    public String updatePassword(@AuthenticationPrincipal UserDetails userDetails,
//                                 @RequestParam String oldPassword,
//                                 @RequestParam String newPassword,
//                                 Model model) {
//        User user = userService.findByEmail(userDetails.getUsername());
//        try {
//            userService.updatePassword(user, oldPassword, newPassword);
//            model.addAttribute("message", "Password has been updated");
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("error", "Error updating password: " + e.getMessage());
//        }
//        return "update-password";
//    }

    @PatchMapping("/update-password")
    public ResponseEntity<MessageResponse> updatePassword(@RequestBody Map<String, String> request) {
        //this shit is weir as f
        //Consider using separate request parameters instead of a Map for clarity.**
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Both oldPassword and newPassword must be provided"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userService.findById(userDetails.getId());

        try {
            userService.updatePassword(user, oldPassword, newPassword);
            return ResponseEntity.ok().body(new MessageResponse("Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }
}
