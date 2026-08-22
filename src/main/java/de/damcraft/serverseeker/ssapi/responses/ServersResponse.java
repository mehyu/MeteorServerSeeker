package de.damcraft.serverseeker.ssapi.responses;

import java.util.List;

public class ServersResponse {
    public String error;
    public int page;
    public int pageSize;
    public int count;
    public int total;
    public int totalPages;
    public String sort;
    public List<Server> servers;

    public boolean isError() {
        return error != null;
    }
}
