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

    @GetMapping({"/user/login", "/user-login"})
    public String userLogin() {
        return "user-login";
    }

    @GetMapping({"/user/register", "/user-register"})
    public String userRegister() {
        return "register";
    }

    @GetMapping({"/user/dashboard", "/user-dashboard"})
    public String userDashboard() {
        return "user-dashboard";
    }

    @GetMapping({"/admin/login", "/admin-login"})
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping({"/admin/dashboard", "/admin-dashboard"})
    public String adminDashboard() {
        return "officer";
    }

    @GetMapping("/officer")
    public String officerDashboard() {
        return "officer";
    }

    // Separate User Sub-Pages
    @GetMapping({"/user/track", "/user-track"})
    public String userTrack() {
        return "user-track";
    }

    @GetMapping({"/user/profile", "/user-profile"})
    public String userProfile() {
        return "user-profile";
    }

    @GetMapping({"/user/notifications", "/user-notifications"})
    public String userNotifications() {
        return "user-notifications";
    }

    // Separate Admin Sub-Pages
    @GetMapping({"/admin/complaints", "/admin-complaints"})
    public String adminComplaints() {
        return "admin-complaints";
    }

    @GetMapping({"/admin/evidence", "/admin-evidence"})
    public String adminEvidence() {
        return "admin-evidence";
    }

    @GetMapping({"/admin/platform-action", "/admin-platform-action"})
    public String adminPlatformAction() {
        return "admin-platform-action";
    }

    @GetMapping({"/admin/reports", "/admin-reports"})
    public String adminReports() {
        return "admin-reports";
    }

    @GetMapping({"/report", "/user/report", "/user-report"})
    public String report() {
        return "report";
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
