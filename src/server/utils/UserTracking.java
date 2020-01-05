package server.utils;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Iterator;

public class UserTracking {

    private ArrayList<RegisterClientModel> registeredClients; // ficheiro
    private ArrayList<MiddleClientModel> loggedClients;

    public UserTracking() {
        registeredClients = new ArrayList<>();
        loggedClients = new ArrayList<>();
    }

    public synchronized boolean loginMiddleClient(String location){
        MiddleClientModel mcm = (MiddleClientModel) this.getRegisteredMiddleClient(location);
        if (mcm == null)
            return false; 
        return loggedClients.add(mcm);
    }

    public synchronized boolean logoutMiddleClient(String location){
        MiddleClientModel mcm = this.getLoggedMiddleClient(location);
        if (mcm == null)
            return false; 
        return loggedClients.remove(mcm);
    }

    public Iterator<MiddleClientModel> getLoggedMiddleClientIterator(){
        return loggedClients.iterator();
    }

    public MiddleClientModel getLoggedMiddleClient(String locationName) {
        Iterator<MiddleClientModel> it = loggedClients.iterator();
        while (it.hasNext()) {
            MiddleClientModel next = it.next();
            if (locationName.equals(next.getLocationName()))
                return next;
        }
        return null;
    }

    public synchronized boolean checkPassword(String password, String location){
        for(int ix=0; ix<registeredClients.size(); ix++) {
            if (location.equals(registeredClients.get(ix).getLocationName()))
                return password.equals(registeredClients.get(ix).getPassword());
        }
        return false;
    }

    public synchronized boolean checkUserClear(String username){
        for(int ix=0; ix<registeredClients.size(); ix++){
            if(username.equals(registeredClients.get(ix).getLocationName()))
                return false;
        }
        return true;
    }

    public MiddleClientModel getRegisteredMiddleClient(String locationName) {
        Iterator<RegisterClientModel> it = registeredClients.iterator();
        while (it.hasNext()) {
            RegisterClientModel next = it.next();
            if (locationName.equals(next.getLocationName()))
                return (MiddleClientModel) next;
        }
        return null;
    }

    //it does the same as the Middle_Client version of this class
    public synchronized boolean addRegisteredMiddleClient(RegisterClientModel client) {
        if (getRegisteredMiddleClient(client.getLocationName()) != null)
            return false;
        return registeredClients.add(client);
    }

    //percorrer para ler o ficheiro
    public synchronized void setRegisteredClients(JsonElement file){
        JsonArray clients
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());
        if (clients != null) {
            int len = clients.size();
            for (int i=0;i<len;i++){
                JsonObject client = clients.get(i).getAsJsonObject();
                Iterator<JsonElement> rUsersIt = client.get("registeredUsers").getAsJsonArray().iterator();
                ArrayList<String> registeredUsers = new ArrayList<>();
                while (rUsersIt.hasNext())
                    registeredUsers.add(rUsersIt.next().getAsString());
                RegisterClientModel mcm = new RegisterClientModel(client.get("password").getAsString(), client.get("locationName").getAsString(), 
                            client.get("multicastAddress").getAsString(), registeredUsers);
                registeredClients.add(mcm);
            }
        }

    }

    public Iterator<RegisterClientModel> getAllRegisteredClientsIterator(){
        return registeredClients.iterator();
    }
}