package com.Nguyen.blogplatform.controller.Authentication;

import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.service.RoleServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role")
public class RoleController {
    private final RoleServices roleServices;

    public RoleController(RoleServices roleServices) {
        this.roleServices = roleServices;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRole(){
        List<Role> role= roleServices.findAllRole();
        return ResponseEntity.ok(role);
    }

    @PostMapping("/add")
    public ResponseEntity<Role> createRole(@RequestBody Role role){
        Role newRole= roleServices.saveRole(role);
        return ResponseEntity.ok(newRole );
    }
}
