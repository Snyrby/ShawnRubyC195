/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import static Controller.CustomersController.returnSelectedCustomer;
import static Controller.LoginScreenController.returnUserName;
import Model.Appointments;
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
import java.time.LocalDate;
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
public class ModifyAppointmentController implements Initializable
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
    @FXML private TextField appointmentIDTextField;
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

        startHours.setOnAction((event) -> 
        {
            checkForEndTime();        
        });     
    }  
    
    /**
     * reverts back to previous page if user presses cancel button
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
     * function that fills the combo box with all times
     */
    private void fillHourComboBox()
    {
        for(int i = 8; i < 22; i++)
        {
            if(i < 10)
            {
                startHours.getItems().add("0" + String.valueOf(i) + ":00");
                startHours.getItems().add("0" + String.valueOf(i) + ":15");
                startHours.getItems().add("0" + String.valueOf(i) + ":30");
                startHours.getItems().add("0" + String.valueOf(i) + ":45");
            }
            else
            {
                startHours.getItems().add(String.valueOf(i) + ":00");
                startHours.getItems().add(String.valueOf(i) + ":15");
                startHours.getItems().add(String.valueOf(i) + ":30");
                startHours.getItems().add(String.valueOf(i) + ":45");
            }          
        }      
    }

    /**
     * runs a sql statement that adds all contact names to the contact combobox
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
     * function that formats the end time to add the start time and appointment length together
     */
    private void formatTime() 
    {
        //gets user input for start time
        String selectedStartTime = startHours.getSelectionModel().getSelectedItem();
        
        //splits the string between hours and minutes
        String[] parts = selectedStartTime.split(":");
        
        switch (appointmentLength.getSelectionModel().getSelectedIndex()) 
        {
            //if the user selects 15 minutes then adds 15 minutes
            case 0:
                {
                    String formatAppointmentLength = parts[1];
                    int time = Integer.parseInt(formatAppointmentLength) + 15;
                    endTime = parts[0] + ":" + String.valueOf(time) + ":00";
                    break;
                }
            //if the user selects 30 minutes then adds 30 minutes
            case 1:
                {
                    String formatAppointmentLength = parts[1];
                    int time = Integer.parseInt(formatAppointmentLength) + 30;
                    endTime = parts[0] + ":" + String.valueOf(time) + ":00";
                    break;
                }
            //if the user selects 45 minutes then adds 45 minutes   
            case 2:
                {
                    String formatAppointmentLength = parts[1];
                    int time = Integer.parseInt(formatAppointmentLength) + 45;
                    endTime = parts[0] + ":" + String.valueOf(time) + ":00";
                    break;
                }
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
        titleLabel.setVisible(false);
        descriptionLabel.setVisible(false);
        locationLabel.setVisible(false);
        typeLabel.setVisible(false);
        appointmentLengthLabel.setVisible(false);
        contactNameLabel.setVisible(false);
        startTimeLabel.setVisible(false);
        boolean valid = true;       
        
        titleInput = titleTextField.getText();
        if(titleInput.isBlank() || Character.isWhitespace(titleInput.charAt(0)) || titleInput.length() > 50)
        {
            valid = false;
            titleLabel.setVisible(true);
            titleTextField.setText("");
            titleTextField.requestFocus();
        }

        descriptionInput = descriptionTextField.getText();
        if(descriptionInput.isBlank()|| Character.isWhitespace(descriptionInput.charAt(0)) || descriptionInput.length() > 50)
        {
            valid = false;
            descriptionLabel.setVisible(true);
            descriptionTextField.setText("");
            descriptionTextField.requestFocus();
        }
         
        locationInput = locationTextField.getText();
        if(locationInput.isBlank()|| locationInput.length() > 50 || Character.isWhitespace(locationInput.charAt(0)))
        {
            valid = false;
            locationLabel.setVisible(true);
            locationTextField.setText("");
            locationTextField.requestFocus();
        }
        
        typeInput = typeTextField.getText();
        if(typeInput.isBlank() || typeInput.length() > 50 || Character.isWhitespace(typeInput.charAt(0)))
        {     
            valid = false;
            typeTextField.setText("");
            typeTextField.requestFocus();
            typeLabel.setVisible(true);            
        }
        
        if(startDate.getValue() == null)
        {
            valid = false;
            startDate.requestFocus();
            startTimeLabel.setVisible(true);
        }
        
        if(startHours.getSelectionModel().isEmpty())
        {
            valid = false;
            startHours.requestFocus();
            startTimeLabel.setVisible(true);
        }
        
        if(appointmentLength.getSelectionModel().isEmpty())
        {
            valid = false;
            appointmentLength.requestFocus();
            appointmentLengthLabel.setVisible(true);
        }
        
        if(contactName.getSelectionModel().isEmpty())
        {
            valid = false;
            contactName.requestFocus();
            contactNameLabel.setVisible(true);
        } 
        
        formatTime();
        formatDates();
        
        try
        {
            String sql = "SELECT Appointment_ID, Start, End FROM appointments WHERE Customer_ID = '" + customerID + "' OR Contact_ID = '" + contactID + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                int appointmentID = rs.getInt("Appointment_ID");
                if(appointmentID != Integer.valueOf(appointmentIDTextField.getText()))
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
                
                    if(appointmentStartDate.before(previousEndTime)
                            && appointmentStartDate.after(previousStartTime)
                            || appointmentStartDate.equals(previousStartTime) 
                            || appointmentStartDate.equals(previousEndTime))
                    {
                        valid = false;
                        startDate.requestFocus();
                        startDate.setValue(null);
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
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        return valid;
    }  
    
    /**
     * function that is ran if the user presses the modify appointment button
     * @throws ParseException 
     */
    public void updateAppointmentButtonPushed() throws ParseException
    {
        if(inputValidation())
        {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant instant = timestamp.toInstant();
            Timestamp tsFromInstant = Timestamp.from(instant);
            Users userName = returnUserName();
            int userID = userName.getUserID();
            getContactID();
            
            //updates appointment based on user input
            try
            {
                int input = JOptionPane.showConfirmDialog(null, "Do you want to update an appointment for " 
                        + selectedCustomer.getCustomerName() + "?");
                if(input == 0)
                {
                    String sql = "UPDATE appointments SET Title = '" + titleInput + "', Description = '" + descriptionInput + "', Location = '" + locationInput + "', Type = '" + typeInput + "', Start = '" 
                            + convertToUTCFromEST(appointmentStartDate) + "', End = '" + convertToUTCFromEST(appointmentEndDate) + "', Last_Update = '" + convertToUTC(tsFromInstant) + "', Last_Updated_By = '" + userName.getUserName() + "', User_ID = '" + userID
                            + "', Contact_ID = '" + contactID + "' WHERE Appointment_ID = '" + appointmentIDTextField.getText() + "';";
                    PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                    ps.executeUpdate();
                    appointmentIDTextField.setText("");
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
     * retrieves info from selected appointment to be used to fill data in modify screen
     * @param appointmentToModify 
     */
    public void initData(Appointments appointmentToModify) 
    {
        Appointments appointment = appointmentToModify;
        appointmentIDTextField.setText(String.valueOf(appointment.getAppointmentID()));
        titleTextField.setText(appointment.getTitle());
        descriptionTextField.setText(appointment.getDescription());
        locationTextField.setText(appointment.getLocation());
        typeTextField.setText(appointment.getType());
        String modifyDate = appointment.getStartTime();
        String[] parts = modifyDate.split(" ");
        LocalDate localDate = LocalDate.parse(parts[0]);
        startDate.setValue(localDate);
        
        //formats time
        String startTimeString = parts[1];
        parts = startTimeString.split(":");
        
        int startHour = Integer.parseInt(parts[0]) + 7;
        int startMinute = Integer.parseInt(parts[1]);
        String startHourString;
        if(startHour < 10)
        {
            startHourString = "0" + String.valueOf(startHour);
        }
        else
        {
            startHourString = String.valueOf(startHour);
        }
       
        modifyDate = appointment.getEndTime();
        parts = modifyDate.split(" ");
        String endTimeString = parts[1];
        parts = endTimeString.split(":");
        
        int endHour = Integer.parseInt(parts[0]) + 7;
        int endMinute = Integer.parseInt(parts[1]);
        
        if(endHour - startHour == 1)
        {
            if(startMinute == endMinute)
            {
                startHours.setValue(startHourString + ":" + String.valueOf(endMinute) + "0");
                appointmentLength.setValue("1 Hour");
            }
            else if (startMinute > endMinute)
            {
                int minuteDisplay = 60 - startMinute;
                startHours.setValue(startHourString + ":" + String.valueOf(startMinute));
                appointmentLength.setValue(minuteDisplay + " Minutes");
            }
            else
            {
                startMinute -= endMinute;
                startHours.setValue(startHourString + ":" + String.valueOf(endMinute));
                appointmentLength.setValue(String.valueOf(startMinute) + " Minutes");
            }
        }
        else
        {
            endMinute -= startMinute;
            if(startMinute == 0)
            {
                startHours.setValue(startHourString + ":" + String.valueOf(startMinute) + "0");
                appointmentLength.setValue(String.valueOf(endMinute) + " Minutes");
            }
            else
            {
                startHours.setValue(startHourString + ":" + String.valueOf(startMinute));
                appointmentLength.setValue(String.valueOf(endMinute) + " Minutes");
            }
            
        }
        contactName.setValue(appointment.getContactName());
        if(startTimeString.equals("21:15:00"))
        {
            appointmentLength.getItems().add("15 Minutes");
            appointmentLength.getItems().add("30 Minutes");
            appointmentLength.getItems().add("45 Minutes");
        }
        else if(startTimeString.equals("21:30:00"))
        {
            appointmentLength.getItems().add("15 Minutes");
            appointmentLength.getItems().add("30 Minutes");
        }
        else if(startTimeString.equals("21:45:00"))
        {
            appointmentLength.getItems().add("15 Minutes");
        }
        else
        {
            appointmentLength.getItems().add("15 Minutes");
            appointmentLength.getItems().add("30 Minutes");
            appointmentLength.getItems().add("45 Minutes");
            appointmentLength.getItems().add("1 Hour");
        }
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
        
        if(startHours.getSelectionModel().getSelectedItem().equals("21:15"))
        {
            appointmentLength.getItems().remove("1 Hour");
        }
        else if(startHours.getSelectionModel().getSelectedItem().equals("21:30"))
        {
            appointmentLength.getItems().remove("1 Hour");
            appointmentLength.getItems().remove("45 Minutes");
        }
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
