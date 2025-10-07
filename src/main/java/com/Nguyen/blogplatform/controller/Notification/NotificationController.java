package com.Nguyen.blogplatform.controller.Notification;



import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.NotificationService;
import com.Nguyen.blogplatform.service.UserServices;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
        

}


