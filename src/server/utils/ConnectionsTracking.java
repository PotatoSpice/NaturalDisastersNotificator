package server.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionsTracking {

    private ArrayList<String> activeMulticastAddresses;
    private int two = 0, three = 0, four = 1, count = 0;
    private final int MAX_ADDRESS_COUNT = 255 * 255 * 254;
    private final int GENERIC_PORT = 65534; // this port can be the same for every location since the IP is what matters

    /**
     * Create a new Instance of this class, checking and removing from the storedAddresses {@link java.util.ArrayList}
     * all invalid multicast IP addresses. <br/>
     * <b>IP range: 230.0.0.1 - 230.255.255.254</b>
     * 
     * @param storedAddresses initial multicast addresses list
     */
    public static ConnectionsTracking newInstance(ArrayList<String> storedAddresses) {
        /* String address;
        for (int i = 0; i < storedAddresses.size(); i++) {
            address = storedAddresses.get(i);
            if (!address.matches("^230([.]([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-4])){3}") || address.startsWith("230.0.0.0"))
                storedAddresses.remove(address);
        }*/
        storedAddresses.removeIf(address -> address.startsWith("230.0.0.0") ||
                        !address.matches("230([.]([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-4])){3}(.*)"));
        return new ConnectionsTracking(storedAddresses);
    }

    private ConnectionsTracking(ArrayList<String> storedAddresses) {          
        this.activeMulticastAddresses = storedAddresses;
    }

    /**
     * Adds a location respective multicast port to the list.
     * Returns false when:
     *  - IP is malformed
     *  - IP is not a multicast address;
     *  - port number is invalid;
     *  - address already exists;
     *  - max address number reached;
     *  
     * 
     * @param IPaddress multicast IP address
     * @return true if successful, false otherwise
     */
    public synchronized boolean addMulticastAddress(String IPaddress) {
        try {
            // check if multicast address is valid
            if (!InetAddress.getByName(IPaddress).isMulticastAddress()) {
                return false;
            }
        } catch (UnknownHostException e) {
            return false; // if the IP address is malformed
        } 

        String address = IPaddress + "/" + GENERIC_PORT;
        if (activeMulticastAddresses.contains(address)) {
            return false;
        }
        
        if (!activeMulticastAddresses.add(address)) {
            return false;
        }
        count++;
        return true;
    }

    /**
     * Removes an already active multicast address
     * 
     * @param IPaddress multicast IP address
     * @return true if successful, false otherwise
     */
    public synchronized boolean removeMulticastAddress(String IPaddress) {
        String address = IPaddress + "/" + GENERIC_PORT;
        if (activeMulticastAddresses.remove(address)) {
            return false;
        }
        count--;
        return true;
    }

    /**
     * Gets all active addresses currently stored.
     * 
     * @return Iterator over the address list ("<ip>/<port>")
     */
    public Iterator<String> getMulticastAddresses() {
        return activeMulticastAddresses.iterator();
    }

    /**
     * Generate a new Multicast Address and add it to the list.
     * Format: from 230.0.0.1 to 230.255.255.254
     * 
     * @return the newly generated address
     */
    public String generateMulticastAddress() {
        String address;
        do {
            if (four != 254) {
                four++;
            } else if (three != 255) {
                four = 1;
                three++;
            } else if (two != 255) {
                four = 1;
                three = 0;
                two++;
            } else if (count != MAX_ADDRESS_COUNT) {
                // if list is not full, restart generation and keep trying for an available address
                four = 1;
                three = 0;
                four = 0;
            } else {
                return null;
            }
            address = "230." + two + "." + three + "." + four;
        } while (!this.addMulticastAddress(address));
        return address + "/" + GENERIC_PORT;
    }

}