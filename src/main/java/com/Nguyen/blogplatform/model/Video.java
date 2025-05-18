package com.Nguyen.blogplatform.model;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Video {
    @Id
    private String id;
    private String title;
    private String filePath;
    private String contentType;
    private String url;

    @PrePersist
    public void assignId(){
        if(id == null){
            id = new ULID().nextULID();
        }
    }

}
