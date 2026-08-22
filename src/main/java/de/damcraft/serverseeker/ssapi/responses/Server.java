package de.damcraft.serverseeker.ssapi.responses;

public class Server {
    public String error;
    public String serverip;
    public int port;
    public String version;
    public String software;
    public String rawVersion;
    public String authmode;
    public String motd;
    public int onlinePlayers;
    public int maxPlayers;
    public boolean hasPlayers;
    public String lastSeen;
    public boolean isVanilla;
    public boolean isEmpty;
    public boolean isFull;
    public double playerFillPercent;
    public Geolocation geolocation;

    public static class Geolocation {
        public String country;
        public String countryName;
        public String city;
        public Double latitude;
        public Double longitude;
    }

    public boolean isError() {
        return error != null;
    }

    public String address() {
        return serverip + ":" + port;
    }

    public Boolean cracked() {
        if (authmode == null) return null;
        return switch (authmode) {
            case "offline" -> true;
            case "online" -> false;
            default -> null;
        };
    }
}
