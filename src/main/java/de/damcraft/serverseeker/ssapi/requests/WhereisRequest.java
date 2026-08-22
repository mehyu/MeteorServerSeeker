package de.damcraft.serverseeker.ssapi.requests;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WhereisRequest {
    private String value;

    public void setName(String name) {
        this.value = name;
    }

    public void setUuid(String uuid) {
        this.value = uuid;
    }

    public String url() {
        return "https://data.minescan.xyz/whereis/" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
