package com.Nguyen.blogplatform.domain.auth.api;




import com.Nguyen.blogplatform.domain.user.application.UserServices;
import com.Nguyen.blogplatform.shared.exception.TokenExpiredException;
import com.Nguyen.blogplatform.shared.validation.annotation.WithRateLimitProtection;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/auth")
public class PasswordController {

    @Autowired
    private UserServices userService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    @WithRateLimitProtection
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            userService.sendResetEmail(email);
        } catch (UsernameNotFoundException | MessagingException e) {
            // Same response either way: don't reveal which emails are registered
        }
        model.addAttribute("message", "If an account exists for this email, a reset link has been sent");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    @WithRateLimitProtection
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword, Model model) {
        try {
            userService.resetPassword(token, newPassword);
            model.addAttribute("message", "Password has been reset");
        } catch (TokenExpiredException e) {
            model.addAttribute("error", "Token is invalid or expired");
        }
        return "reset-password";
    }


}
