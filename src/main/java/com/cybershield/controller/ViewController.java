package com.cybershield.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/report")
    public String report() {
        return "report";
    }

    @GetMapping("/officer")
    public String officerDashboard() {
        return "officer";
    }

    @GetMapping("/complaints/view/{id}")
    public String complaintDetails(@PathVariable Long id, Model model) {
        model.addAttribute("complaintId", id);
        return "details";
    }

    @GetMapping("/details")
    public String detailsWithParam(@RequestParam(required = false) Long id, Model model) {
        model.addAttribute("complaintId", id);
        return "details";
    }
}
