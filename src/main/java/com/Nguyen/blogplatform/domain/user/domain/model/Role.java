package com.Nguyen.blogplatform.domain.user.domain.model;



import com.Nguyen.blogplatform.shared.enums.ERole;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Data
@Getter
@Setter

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private ERole name;

    public Role() {

    }

    public Role(ERole name) {
        this.name = name;
    }

    // getters and setters
}