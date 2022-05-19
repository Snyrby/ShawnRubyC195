/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import static Controller.CustomersController.returnSelectedCustomer;
import static Controller.LoginScreenController.returnUserName;
import Model.Customer;
import Model.Users;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import utils.DBConnection;

/*
* FXML Controller class
* @author Shawn Ruby
*/
public class AddAppointmentsController implements Initializable 
{
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label locationLabel;
    @FXML private Label typeLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label appointmentLengthLabel;
    @FXML private Label contactNameLabel;
    @FXML private ComboBox<String> appointmentLength;
    @FXML private ComboBox<String> startHours;
    @FXML private ComboBox<String> contactName;
    @FXML private DatePicker startDate;
    @FXML private TextField titleTextField;
    @FXML private TextField descriptionTextField;
    @FXML private TextField locationTextField;
    @FXML private TextField typeTextField;
    private String endTime;
    private String startTime;
    private Timestamp appointmentEndDate;
    private Timestamp appointmentStartDate;
    
    private String titleInput;
    private String descriptionInput;
    private String locationInput;
    private String typeInput;
    private int contactID;
    private String contactNameInput;
    Customer selectedCustomer = returnSelectedCustomer();
    private int customerID = selectedCustomer.getCustomerID();
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        fillHourComboBox();
        fillContactComboBox();   

        //sets listener to check if the business is about to close (2200 hours)
        startHours.setOnAction((event) -> 
        {
            checkForEndTime();
        });   
    }  
    
    /**
     * sends the user back to previous page if cancel button is pushed
     * @param event
     * @throws IOException 
     */
    public void cancelButtonPushed(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/ViewAppointments.fxml"));
        Parent tableViewParent = loader.load();
        Scene tableViewScene = new Scene(tableViewParent);  
            
        //This line gets the Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(tableViewScene);
        window.setResizable(false);
        window.setTitle("Customer Main Screen");
        window.show();
    }
    
    /**
     * fills the hour combo box for every 15 minute interval from 9 am to 10 pm
     */
    private void fillHourComboBox()
    {
        for(int i = 8; i < 22; i++)
        {
            //adds 0 if before 10 am (0900)
            if(i < 10)
            {
                startHours.getItems().add("0" + String.valueOf(i) + ":00");
                startHours.getItems().add("0" + String.valueOf(i) + ":15");
                startHours.getItems().add("0" + String.valueOf(i) + ":30");
                startHours.getItems().add("0" + String.valueOf(i) + ":45");
            }
            
            //doesn't add 0 if 10 or more
            else
            {
                startHours.getItems().add(String.valueOf(i) + ":00");
                startHours.getItems().add(String.valueOf(i) + ":15");
                startHours.getItems().add(String.valueOf(i) + ":30");
                startHours.getItems().add(String.valueOf(i) + ":45");
            }          
        }
        
        //fills the appointment length combo box with every 15 minutes
        appointmentLength.getItems().add("15 Minutes");
        appointmentLength.getItems().add("30 Minutes");
        appointmentLength.getItems().add("45 Minutes");
        appointmentLength.getItems().add("1 Hour");
    }

    /**
     * fills the contact combobox with all of the contacts from a sql statement
     */
    private void fillContactComboBox() 
    {
        try
        {
            String sql = "SELECT * FROM contacts;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                String Name = rs.getString("Contact_Name");
                contactName.getItems().add(Name);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * format that calculates the end time
     */
    private void formatTime() 
    {
        //retrieves the start time from 
        String selectedStartTime = startHours.getSelectionModel().getSelectedItem();
        
        //splits the string at the ':'
        String[] parts = selectedStartTime.split(":");
        switch (appointmentLength.getSelectionModel().getSelectedIndex()) 
        {
            //if the user selects 15 minutes then adds 15 minutes to the user's start time
            case 0:
                {
                    String formatAppointmentLength = parts[1];
                    int time = Integer.parseInt(formatAppointmentLength) + 15;
                    endTime = parts[0] + ":" + String.valueOf(time) + ":00";
                    break;
                }
            //the same but 30 minutes
            case 1:
                {
                    String formatAppointmentLength = parts[1];
                    int time = Integer.parseInt(formatAppointmentLength) + 30;
                    endTime = parts[0] + ":" + String.valueOf(time) + ":00";
                    break;
                }
            //the same but 45 minutes
            case 2:
                {
                    String formatAppointmentLength = parts[1];
                    int time = Integer.parseInt(formatAppointmentLength) + 45;
                    endTime = parts[0] + ":" + String.valueOf(time) + ":00";
                    break;
                }
            //the same but 1 hour
            case 3:
            {
                String formatAppointmentLength = parts[0];
                int time = Integer.parseInt(formatAppointmentLength) + 1;
                if(time > 9)
                {
                    endTime = String.valueOf(time) + ":" + parts[1] + ":00";
                }
                else
                {
                    endTime = "0" + String.valueOf(time) + ":" + parts[1] + ":00";
                }                   
                break;
            }
            default:
                break;
        }
    }
   
    /**
     * boolean function that checks for validation
     * @return
     * @throws ParseException 
     */
    private boolean inputValidation() throws ParseException 
    {
        //sets all error statements to hidden
        titleLabel.setVisible(false);
        descriptionLabel.setVisible(false);
        locationLabel.setVisible(false);
        typeLabel.setVisible(false);
        appointmentLengthLabel.setVisible(false);
        contactNameLabel.setVisible(false);
        startTimeLabel.setVisible(false);
        
        boolean valid = true;       
        
        //retrieves title from textbox
        titleInput = titleTextField.getText();
        
        //checks if title is blank and less than 50 chars and will alert user if fails validation
        if(titleInput.isEmpty() || Character.isWhitespace(titleInput.charAt(0)) || titleInput.length() > 50)
        {
            valid = false;
            titleLabel.setVisible(true);
            titleTextField.setText("");
            titleTextField.requestFocus();
        }

        //retrieves description from textbox
        descriptionInput = descriptionTextField.getText();
        
        //checks if description is blank and less than 50 chars and will alert user if fails validation
        if(descriptionInput.isBlank()|| Character.isWhitespace(descriptionInput.charAt(0)) || descriptionInput.length() > 50)
        {
            valid = false;
            descriptionLabel.setVisible(true);
            descriptionTextField.setText("");
            descriptionTextField.requestFocus();
        }
         
        //retrieves location from textbox
        locationInput = locationTextField.getText();
        
        //checks if location is blank and less than 50 chars and will alert user if fails validation
        if(locationInput.isBlank()|| locationInput.length() > 50 || Character.isWhitespace(locationInput.charAt(0)))
        {
            valid = false;
            locationLabel.setVisible(true);
            locationTextField.setText("");
            locationTextField.requestFocus();
        }
        
        //retrieves type from textbox
        typeInput = typeTextField.getText();
        
        //checks if type is blank and less than 50 chars and will alert user if fails validation
        if(typeInput.isBlank() || typeInput.length() > 50 || Character.isWhitespace(typeInput.charAt(0)))
        {     
            valid = false;
            typeTextField.setText("");
            typeTextField.requestFocus();
            typeLabel.setVisible(true);            
        }
        
        //if the user does not pick a date then alerts user
        if(startDate.getValue() == null)
        {
            valid = false;
            startDate.requestFocus();
            startTimeLabel.setVisible(true);
        }
        
        //if the user does not pick a start time then alerts user
        if(startHours.getSelectionModel().isEmpty())
        {
            valid = false;
            startHours.requestFocus();
            startTimeLabel.setVisible(true);
        }
        
        //if the user does not pick a appointment length then alerts user
        if(appointmentLength.getSelectionModel().isEmpty())
        {
            valid = false;
            appointmentLength.requestFocus();
            appointmentLengthLabel.setVisible(true);
        }
        
        //if the user does not pick a contact name then alerts user
        if(contactName.getSelectionModel().isEmpty())
        {
            valid = false;
            contactName.requestFocus();
            contactNameLabel.setVisible(true);
        } 
        
        //calls function to format date and times
        if(valid == true)
        {
            formatTime();
            formatDates();
            
            //runs sql statement to retrieve start and end times
            try
            {
                String sql = "SELECT Start, End FROM appointments WHERE Customer_ID = '" + customerID + "' OR Contact_ID = '" + contactID + "';";
                PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
            
                //retrieves info
                while(rs.next())
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String stringPreviousStartTime = rs.getString("Start");
                    String stringPreviousEndTime = rs.getString("End");
                    Date parsedDate = dateFormat.parse(stringPreviousStartTime);
                    Timestamp previousStartTime = new java.sql.Timestamp(parsedDate.getTime());
                    parsedDate = dateFormat.parse(stringPreviousEndTime);
                    Timestamp previousEndTime = new java.sql.Timestamp(parsedDate.getTime());
                    previousStartTime = Timestamp.valueOf(convertToEST(previousStartTime));
                    previousEndTime = Timestamp.valueOf(convertToEST(previousEndTime));
                
                    //validates if the user has any overlapping appointments and will alert user if the appointment overlaps
                    if(appointmentStartDate.before(previousEndTime) 
                            && appointmentStartDate.after(previousStartTime) 
                            || appointmentStartDate.equals(previousStartTime)
                            || appointmentStartDate.equals(previousEndTime))
                    {
                        valid = false;
                        startDate.requestFocus();
                        startDate. setValue(null);
                        startHours.setValue("");
                        infoBox("THAT APPOINTMENT OVERLAPS WITH ANOTHER APPOINTMENT!", "ERROR");
                        break;
                    }
                    else if(appointmentEndDate.before(previousEndTime)
                            && appointmentEndDate.after(previousStartTime)
                            || appointmentEndDate.equals(previousEndTime)
                            || appointmentEndDate.equals(previousStartTime))
                    {
                        valid = false;
                        startDate.requestFocus();
                        startDate.setValue(null);
                        startHours.setValue("");
                        infoBox("THAT APPOINTMENT OVERLAPS WITH ANOTHER APPOINTMENT!", "ERROR");
                        break;
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return valid;
    }  
    
    
    /**
     * function that is ran if the user presses the add appointment button
     * @throws ParseException 
     */
    public void addAppointmentButtonPushed() throws ParseException
    {
        //runs inputvalidation function
        if(inputValidation())
        {
            //creates user's local time
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant instant = timestamp.toInstant();
            Timestamp tsFromInstant = Timestamp.from(instant);
            
            //gets the userinfo and retrieves userid
            Users userName = returnUserName();
            int userID = userName.getUserID();
            getContactID();
            
            try
            {
                //asks user for confirmation on adding appointment and adds appointment if user confirms then sets all values to null
                int input = JOptionPane.showConfirmDialog(null, "Do you want to add an appointment for " 
                        + selectedCustomer.getCustomerName() + "?");
                if(input == 0)
                {
                    //adds appointment to sql server 
                    String sql = "INSERT INTO appointments (Title, Description, Location, Type, Start, End, Create_Date, "
                            + "Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID)"
                            + "VALUES('" + titleInput + "', '" + descriptionInput + "', '" + locationInput + "', '" + typeInput + "', '" + convertToUTCFromEST(appointmentStartDate) + "', '" + convertToUTCFromEST(appointmentEndDate) + "', '" 
                            + convertToUTC(tsFromInstant) + "', '" + userName.getUserName() + "', '" + convertToUTC(tsFromInstant) + "', '" + userName.getUserName() + "', " + customerID + ", " + userID 
                            + ", " + contactID + ");";
                    PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                    ps.executeUpdate();
                    
                    //sets all the values to null
                    titleTextField.setText("");
                    descriptionTextField.setText("");
                    locationTextField.setText("");
                    typeTextField.setText("");
                    startDate.setValue(null);
                    startHours.setValue("");
                    appointmentLength.setValue("");
                    contactName.setValue("");
                }          
            }
            catch (SQLException e)
            {
            e.printStackTrace();
            }
        }
    }

    /**
     * gets the contact id based on the contact name the user selected
     */
    private void getContactID() 
    {
        contactNameInput = contactName.getSelectionModel().getSelectedItem();
        try
        {
            String sql = "SELECT Contact_ID FROM contacts WHERE Contact_Name = '" + contactNameInput + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                 contactID = rs.getInt("Contact_ID");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * formats the date to be added into the server
     */
    private void formatDates() 
    {
        endTime = startDate.getValue().toString() + " " + endTime;
        startTime = startDate.getValue().toString() + " " + startHours.getSelectionModel().getSelectedItem() + ":00";
        try 
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = dateFormat.parse(endTime);
            appointmentEndDate = new java.sql.Timestamp(parsedDate.getTime());
            parsedDate = dateFormat.parse(startTime);
            appointmentStartDate = new java.sql.Timestamp(parsedDate.getTime());
        } 
        catch(Exception e) 
        { 
            e.printStackTrace();
        }
    }
    
    /**
     * function to easy access a infobox
     * @param infoMessage
     * @param titleBar 
     */
    private static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * checks if the new appointment goes over the close time
     */
    private void checkForEndTime() 
    {
        appointmentLength.getItems().clear();
        appointmentLength.getItems().add("15 Minutes");
        appointmentLength.getItems().add("30 Minutes");
        appointmentLength.getItems().add("45 Minutes");
        appointmentLength.getItems().add("1 Hour");
        
        //if the user selects 21:15 then removes 1 hour 
        if(startHours.getSelectionModel().getSelectedItem().equals("21:15"))
        {
            appointmentLength.getItems().remove("1 Hour");
        }
        
        //if user selects 21:30 then removes 45 mins and 1 hour
        else if(startHours.getSelectionModel().getSelectedItem().equals("21:30"))
        {
            appointmentLength.getItems().remove("1 Hour");
            appointmentLength.getItems().remove("45 Minutes");
        }
        
        //if user selects 21:45 then onlpy leaves 15 minutes as an available option
        else if(startHours.getSelectionModel().getSelectedItem().equals("21:45"))
        {
            appointmentLength.getItems().remove("30 Minutes");
            appointmentLength.getItems().remove("45 Minutes");
            appointmentLength.getItems().remove("1 Hour");
        }
    }        
    
    /**
     * converts a timestamp to UTC and converts to a formatted string
     * @param time
     * @return String
     */
    private String convertToUTC(Timestamp time)
    {
        LocalDateTime ldt = time.toLocalDateTime();
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(ZoneId.systemDefault().toString()));
        ZonedDateTime utczdt = zdt.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime ldtIn = utczdt.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = ldtIn.format(formatter); 
        
        return formatDateTime;
    }
    
    /**
     * converts a timestamp to EST and converts to a formatted string
     * @param time
     * @return String
     */
    private String convertToEST(Timestamp time)
    {
        ZonedDateTime zdtOut = time.toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtOutToLocalTZ = zdtOut.withZoneSameInstant(ZoneId.of("America/New_York"));
        LocalDateTime ldtOutFinal = zdtOutToLocalTZ.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = ldtOutFinal.format(formatter); 
        
        return formatDateTime;   
    }
    
     /**
     * converts a timestamp to UTC and converts to a formatted string
     * @param time
     * @return String
     */
    private String convertToUTCFromEST(Timestamp time)
    {
        LocalDateTime ldt = time.toLocalDateTime();
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(ZoneId.of("America/New_York").toString()));
        ZonedDateTime utczdt = zdt.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime ldtIn = utczdt.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = ldtIn.format(formatter); 
        
        return formatDateTime;
    }
}
