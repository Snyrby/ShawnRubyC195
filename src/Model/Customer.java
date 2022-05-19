/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/*
* @author Shawn Ruby
*/
public class Customer 
{
    private int customerID;
    private String customerName;
    private String Address;
    private String ZipCode;
    private String PhoneNumber;
    private String createDate;
    private String createdBy;
    private String lastUpdate;
    private String lastUpdatedBy;
    private int divisionID;
    private String State;
    private String Country;
    
    public Customer(int customerID, String customerName, String Address, String ZipCode, 
            String PhoneNumber, String createDate, String createdBy, String lastUpdate, 
            String lastUpdatedBy, int divisionID, String State, String Country)
    {
        setCustomerID(customerID);
        setCustomerName(customerName);
        setAddress(Address);
        setZipCode(ZipCode);
        setPhoneNumber(PhoneNumber);
        setCreateDate(createDate);
        setCreatedBy(createdBy);
        setLastUpdate(lastUpdate);
        setLastUpdatedBy(lastUpdatedBy);
        setDivisionID(divisionID);
        setState(State);
        setCountry(Country);
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getZipCode() {
        return ZipCode;
    }

    public void setZipCode(String ZipCode) {
        this.ZipCode = ZipCode;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String PhoneNumber) {
        this.PhoneNumber = PhoneNumber;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public int getDivisionID() {
        return divisionID;
    }

    public void setDivisionID(int divisionID) {
        this.divisionID = divisionID;
    }
    
    public String getState(){
        return State;
    }
    
    public void setState(String State){
        this.State = State;
    }
    
    public String getCountry(){
        return Country;
    }
    
    public void setCountry(String Country){
        this.Country = Country;
    }
}
