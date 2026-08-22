package de.damcraft.serverseeker.ssapi.requests;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServersRequest {
    public enum Software {
        Any,
        Paper,
        Purpur,
        Spigot,
        Bukkit,
        Fabric,
        Forge,
        Quilt,
        Velocity,
        Waterfall,
        Vanilla
    }

    private String version;
    private String software;
    private String authmode;
    private String country;
    private Integer minPlayers;
    private String sort;

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSoftware(Software software) {
        this.software = software == null || software == Software.Any ? null : software.name().toLowerCase();
    }

    public void setAuthmode(String authmode) {
        this.authmode = authmode;
    }

    public void setCountryCode(String cc) {
        this.country = cc == null || cc.equalsIgnoreCase("any") ? null : cc.toLowerCase();
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String url() {
        List<String> params = new ArrayList<>();
        if (version != null) params.add("version=" + encode(version));
        if (software != null) params.add("software=" + encode(software));
        if (authmode != null) params.add("authmode=" + encode(authmode));
        if (country != null) params.add("country=" + encode(country));
        if (minPlayers != null) params.add("minPlayers=" + minPlayers);
        if (sort != null) params.add("sort=" + encode(sort));

        if (params.isEmpty()) return "https://data.minescan.xyz/servers";
        return "https://data.minescan.xyz/servers?" + String.join("&", params);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
