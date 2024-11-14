package com.Nguyen.blogplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FController {

    @GetMapping("/fuck")
    public String fuck(Model model) {
        String message = "Fuck you";
        model.addAttribute("message", message);
        model.addAttribute("count", 1000);
        return "fuck";
    }
}
