/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 * @author Shawn Ruby
 */
public class Contacts 
{
    private String contactName;
    private int appointmentID;
    private String Title;
    private String Type;
    private String Description;
    private String Start;
    private String End;
    private int customerID;
    
    public Contacts(String contactName, int appointmentID, String Title, String Type, String Description, String Start, String End, int customerID)
    {
        setContactName(contactName);
        setAppointmentID(appointmentID);
        setTitle(Title);
        setType(Type);
        setDescription(Description);
        setStart(Start);
        setEnd(End);
        setCustomerID(customerID);   
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getStart() {
        return Start;
    }

    public void setStart(String Start) {
        this.Start = Start;
    }

    public String getEnd() {
        return End;
    }

    public void setEnd(String End) {
        this.End = End;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }
}
