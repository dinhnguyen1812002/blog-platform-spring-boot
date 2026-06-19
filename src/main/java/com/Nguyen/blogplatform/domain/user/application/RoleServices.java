package com.Nguyen.blogplatform.domain.user.application;



import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServices {

    private final RoleRepository roleRepo;


    public RoleServices(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    public List<Role> findAllRole(){
            return roleRepo.findAll();
    }
    public Role saveRole(Role role){
        return roleRepo.save(role);
    }
    public void deleteRole(Integer id){
         roleRepo.deleteById(id);
    }

}
