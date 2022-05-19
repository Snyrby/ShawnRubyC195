/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 * @author Shawn Ruby
 */
public class Location 
{
    private String customerName;
    private String Location;
    private int locationTotal;
    
    public Location(String customerName, String Location, int locationTotal)
    {
        setCustomerName(customerName);
        setLocation(Location);
        setLocationTotal(locationTotal);
    }
    
    public String getLocation() {
        return Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public int getLocationTotal() {
        return locationTotal;
    }

    public void setLocationTotal(int locationTotal) {
        this.locationTotal = locationTotal;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
