package com.cybershield.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlatformContactConfig {

    private final Map<String, String> platformContacts = new ConcurrentHashMap<>();

    public PlatformContactConfig() {
        // Default standard safety & moderation contact addresses for digital platforms
        platformContacts.put("Instagram", "safety-reports@instagram.com");
        platformContacts.put("WhatsApp", "support@whatsapp.com");
        platformContacts.put("Facebook", "safety-reports@meta.com");
        platformContacts.put("YouTube", "safety@youtube.com");
        platformContacts.put("X", "safety@x.com");
        platformContacts.put("Discord", "safety@discordapp.com");
        platformContacts.put("Online Game", "safety-moderation@gaming-network.org");
        platformContacts.put("Other", "safety-reports@platform.org");
    }

    public String getContactForPlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return platformContacts.getOrDefault("Other", "safety-reports@platform.org");
        }
        for (Map.Entry<String, String> entry : platformContacts.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(platform.trim())) {
                return entry.getValue();
            }
        }
        return platformContacts.getOrDefault("Other", "safety-reports@platform.org");
    }

    public Map<String, String> getAllContacts() {
        return platformContacts;
    }

    public void updateContact(String platform, String email) {
        if (platform != null && !platform.isBlank() && email != null && !email.isBlank()) {
            platformContacts.put(platform.trim(), email.trim());
        }
    }
}
