package com.Nguyen.blogplatform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FController {

    @GetMapping("/fuck")
    public String fuck(Model model) {
        String message = "Fuck you";
        String txt ="vào dđay làm gig";
        model.addAttribute("message", message);
        model.addAttribute("count", 1000);
        return "fuck";
    }




    @GetMapping("/get")
    public ResponseEntity<List<String>> get() {
        List<String> list = new ArrayList<String>();
        list.add("Hello");
        list.add("World");
        list.add("Hello World");
        list.add("???");
        String msg = "This is build";
        return ResponseEntity.ok(list);
    }

    @GetMapping("/")
    public String testString(){
        return "notification";
    }

    @GetMapping("/traffic")
    public String traffic(){
        return "traffic-area-chart";
    }

}
