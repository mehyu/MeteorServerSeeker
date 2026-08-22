package de.damcraft.serverseeker.ssapi.requests;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ServerInfoRequest {
    private String ip;
    private Integer port;

    public void setIpPort(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public String url() {
        StringBuilder sb = new StringBuilder("https://data.minescan.xyz/who/" + URLEncoder.encode(ip, StandardCharsets.UTF_8));
        if (port != null) sb.append("?port=").append(port);
        return sb.toString();
    }
}
