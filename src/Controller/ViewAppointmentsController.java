/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import static Controller.CustomersController.returnSelectedCustomer;
import Model.Appointments;
import Model.Customer;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import utils.DBConnection;

/**
 * FXML Controller class
 * @author Shawn Ruby
 */
public class ViewAppointmentsController implements Initializable 
{
    @FXML private TableView<Appointments> appointmentTableView;
    @FXML private TableColumn<Appointments, String> appointmentIdColumn;
    @FXML private TableColumn<Appointments, String> titleColumn;
    @FXML private TableColumn<Appointments, String> descriptionColumn;
    @FXML private TableColumn<Appointments, String> locationColumn;
    @FXML private TableColumn<Appointments, String> typeColumn;
    @FXML private TableColumn<Appointments, String> startColumn;
    @FXML private TableColumn<Appointments, String> endColumn;
    @FXML private TableColumn<Appointments, String> contactColumn;
    @FXML private TableColumn<Appointments, String> customerIdColumn;
    @FXML private TableColumn<Appointments, String> contactIdColumn;
    
    @FXML private ComboBox<String> contactComboBox;
    
    @FXML private Label startText;
    
    public static ObservableList<String> allMonthNames = FXCollections.observableArrayList("January", "February", "March", 
            "April", "May", "June", "July", "August", "September", "October", "November", "December");
    Appointments appointment;
    private ObservableList<Appointments> allAppointments = FXCollections.observableArrayList();
    private ObservableList<Appointments> allContactAppointments = FXCollections.observableArrayList();
    private ObservableList<Appointments> allAppointmentsInOneWeek = FXCollections.observableArrayList();
    private ObservableList<Appointments> allAppointmentsInOneMonth = FXCollections.observableArrayList();
    private int contactID;
    private String contactName;
    private String customerName;
    private int customerID;
    int contactSelectedID;
    
    private ToggleGroup filterRadioButtons;
    @FXML private RadioButton weekRadioButton;
    @FXML private RadioButton monthRadioButton;
    @FXML private RadioButton allRadioButton;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //Configures the radio buttons so only 1 can be toggled at a time
        filterRadioButtons = new ToggleGroup();
        this.weekRadioButton.setToggleGroup(filterRadioButtons);
        this.monthRadioButton.setToggleGroup(filterRadioButtons);
        this.allRadioButton.setToggleGroup(filterRadioButtons);
        
        //sets table columns
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("appointmentID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("Title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("Description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("Location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("Type"));
        startColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("startTime"));
        endColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("endTime"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("contactName"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("customerID"));
        contactIdColumn.setCellValueFactory(new PropertyValueFactory<Appointments, String>("contactID"));
        
        //retrieves the customer info
        retrieveInfo();
        
        //sets the combobox value to view each contact's appointments
        contactComboBox.getItems().add("All appointments");
        contactComboBox.setValue("All appointments");
        
        //gets all appointment info
        getAllAppointments();
        
        //sets up table
        generateAppointmentTable();
        
        //sets up a listener for the filter radio buttons
        filterRadioButtons.selectedToggleProperty().addListener(new ChangeListener<Toggle>() 
        {
            @Override
            public void changed(ObservableValue<? extends Toggle> ob, 
                                                    Toggle o, Toggle n)
            {
                //if month is selected, it will display all appointments in 1 month
                if (monthRadioButton.isSelected())     
                {
                    appointmentTableView.setItems(allAppointmentsInOneMonth);
                    appointmentTableView.refresh();
                    contactComboBox.setValue("All appointments");
                } 
                //if week is selected, it will display all appointments in 1 week
                else if (weekRadioButton.isSelected()) 
                {
                    appointmentTableView.setItems(allAppointmentsInOneWeek);
                    appointmentTableView.refresh();
                    contactComboBox.setValue("All appointments");
                } 
                else 
                {
                    //sets up table
                    generateAppointmentTable();
                }        
            }
        });
        
        //sets the title to display the customer's name and appoointment info
        startText.setText(customerName + " Appointment Info"); 
        
        //sets a listener on the contact combo box to display their appointment info
        contactComboBox.setOnAction((event) -> {      
        if(contactComboBox.getSelectionModel().getSelectedItem().equals("All appointments"))
        {  
            if(allRadioButton.isSelected())
            {
                //sets up table
                generateAppointmentTable();
            }  
        }
        else
        {
            //selects the all appointent radio button and displays contact appointments
            allRadioButton.setSelected(true);
            showContactAppointments();
        }
        });   
        
    }    

    /**
     * retrieves customer info
     */
    public void retrieveInfo() 
    {
        Customer selectedCustomer = returnSelectedCustomer();
        customerName = selectedCustomer.getCustomerName();
        customerID = selectedCustomer.getCustomerID();
    }

    /**
     * sql statement used to retrieve all appointment info and saves as an object
     */
    private void getAllAppointments()
    {
        try
        {
            String sql = "SELECT * FROM appointments WHERE Customer_ID = '" + customerID + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            allAppointments.clear();
            
            while(rs.next())
            {
                int appointmentID = rs.getInt("Appointment_ID");
                String Title = rs.getString("Title");
                String Description = rs.getString("Description");
                String Location = rs.getString("Location");
                String Type = rs.getString("Type");   
                
                Timestamp startTime = rs.getTimestamp("Start");                
                Timestamp endTime = rs.getTimestamp("End");          
                Timestamp createDate = rs.getTimestamp("Create_Date");               
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                customerID = rs.getInt("Customer_ID");
                int userID = rs.getInt("User_ID");
                contactID = rs.getInt("Contact_ID");
                formatContact();
                appointment = new Appointments(appointmentID, Title, Description, Location, Type, convertToLocal(startTime), 
                        convertToLocal(endTime), convertToLocal(createDate), createdBy, convertToLocal(lastUpdate), lastUpdatedBy, 
                        customerID, userID, contactID, contactName);
                allAppointments.add(appointment);
                
                //creates timestamps for now, in 1 week and in 1 month
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime currentDatePlus7Days = now.plusDays(7);
                LocalDateTime currentDatePlus1Month = now.plusMonths(1);
                Timestamp nowTimestamp = Timestamp.valueOf(now);
                Timestamp oneWeek = Timestamp.valueOf(currentDatePlus7Days);
                Timestamp oneMonth = Timestamp.valueOf(currentDatePlus1Month);

                //checks if appointment is in 1 week and adds to the 1 week list
                if(Timestamp.valueOf(convertToLocal(startTime)).before(Timestamp.valueOf(convertToLocal(oneWeek))) && 
                        Timestamp.valueOf(convertToLocal(startTime)).after(Timestamp.valueOf(convertToLocal(nowTimestamp))))
                {
                    allAppointmentsInOneWeek.add(appointment);
                }
                //checks if appointment is in 1 month and adds to the 1 month list
                else if(Timestamp.valueOf(convertToLocal(startTime)).before(Timestamp.valueOf(convertToLocal(oneMonth))) &&
                        Timestamp.valueOf(convertToLocal(startTime)).after(Timestamp.valueOf(convertToLocal(nowTimestamp))))
                {
                     allAppointmentsInOneMonth.add(appointment);
                }
                
                //checks if the contact view combobox contains the name to prevent adding duplicates
                if(contactComboBox.getItems().contains(contactName))
                {                   
                }
                else
                {
                    contactComboBox.getItems().add(contactName);
                }                
            }
        } 
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * alerts the user if there are no appointments. if there are, displays them in table view
     */
    private void generateAppointmentTable()
    {
        if(allAppointments.isEmpty())
        {
            Alert nullalert = new Alert(Alert.AlertType.INFORMATION);
            nullalert.setTitle("Appointment Information");
            nullalert.setHeaderText("No Information to Display");
            nullalert.setContentText("That Customer Doesn't Have Any Appointments!");
            nullalert.showAndWait();
        }
        else
        {
            appointmentTableView.setItems(allAppointments);
            appointmentTableView.refresh();
        }     
    }
    
    /**
     * sql statement that retrieves the contact name
     */
    private void formatContact() 
    {
        try
        {
            String sql = "SELECT Contact_Name FROM contacts WHERE Contact_ID = '" + contactID + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                contactName = rs.getString("Contact_Name");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
  
    /**
     * if the add appointment button is pushed, it runs this function to load the add appointment screen
     * @param event
     * @throws IOException 
     */
    public void addAppointmentButtonPushed(ActionEvent event) throws IOException 
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/AddAppointments.fxml"));
        Parent tableViewParent = loader.load();
        Scene tableViewScene = new Scene(tableViewParent);  
            
        //This line gets the Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(tableViewScene);
        window.setResizable(false);
        window.setTitle("Add Appointment Screen");
        window.show();
    }
    
    /**
     * if the modify appointment button is pushed, it runs this function to display the modify appointment screen
     * @param event
     * @throws IOException 
     */
    public void modifyAppointmentButtonPushed(ActionEvent event) throws IOException
    {
        Appointments appointmentToModify = appointmentTableView.getSelectionModel().getSelectedItem();
        if(appointmentToModify == null)
        {
            infoBox("PLEASE SELECT AN APPOINTMENT TO MODIFY!", "ERROR");
        }
        else
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/View/ModifyAppointment.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent);  
            
            ModifyAppointmentController controller = loader.getController();
            controller.initData(appointmentToModify);
            //This line gets the Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.setResizable(false);
            window.setTitle("Modify Appointment Screen");
            window.show();
        } 
    }
    
    /**
     * runs a sql statement to delete appointment
     */
    public void removeAppointmentButtonPushed()
    {
        //gets user selection
        Appointments appointmentToDelete = appointmentTableView.getSelectionModel().getSelectedItem();
        
        Boolean checkForRemoval = false;
        
        //if null then alerts user
        if(appointmentToDelete == null)
        {
            infoBox("PLEASE SELECT AN APPOINTMENT TO DELETE!", "ERROR");
        }
        else
        {
            try
            {
                //asks user to confirm deletion if user has selected an appointment
                int input = JOptionPane.showConfirmDialog(null, "Do you want to Delete the appointment Id: " + 
                        appointmentToDelete.getAppointmentID() + " for " + customerName + " by " + appointmentToDelete.getContactName() + "?");
                if(input == 0)
                {
                    //runs sql statement to delete this refreshes table
                    String sql = "DELETE FROM appointments WHERE Appointment_ID = '" + appointmentToDelete.getAppointmentID() + "';";
                    PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                    ps.executeUpdate();
                    allAppointments.remove(appointmentToDelete);
                    
                    //loops through to remove the removed appointment from the contact combo box
                    for(Appointments appointment : allAppointments)
                    {
                        if(appointment.getContactName().equals(appointmentToDelete.getContactName()))
                        {  
                            checkForRemoval = false;
                        }
                        else
                        {
                            checkForRemoval = true;
                        }
                    }
                    if(checkForRemoval == true)
                    {
                        contactComboBox.getItems().remove(appointmentToDelete.getContactName());
                    }
                    generateAppointmentTable();     
                }          
            }
            catch (SQLException e)
            {
            e.printStackTrace();
            }
        }
    }
    
    /**
     * if cancel button is pushed then reverts back to previous page
     * @param event
     * @throws IOException 
     */
    public void cancelButtonPushed(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/Customers.fxml"));
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
     * function that sets up info box
     * @param infoMessage
     * @param titleBar 
     */
    private static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * function that will display the appointments for each contact
     */
    private void showContactAppointments() 
    {
        //gets user input
        String contactSelected = contactComboBox.getSelectionModel().getSelectedItem();
       
        //if the user selects all appointments then it displays all appointments
        if (contactSelected.equals("All appointments"))
        {
            generateAppointmentTable();
        }
        else
        {
            //if the user selects a contact
            try
            {
                //runs sql statement to retrieve contact id based on user input
                String sql = "SELECT Contact_ID FROM contacts WHERE Contact_Name = '" + contactSelected + "';";
                PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                
                //retrieves contact id
                while(rs.next())
                {
                    contactSelectedID = rs.getInt("Contact_ID");
                }
                try
                {
                    //with the contactid, runs a sql statement to retrieve all appointment info related to the contact
                    sql = "SELECT * FROM appointments WHERE Contact_ID = '" + contactSelectedID + "' AND Customer_ID = '" + customerID + "';";
                    ps = DBConnection.getConnection().prepareStatement(sql);
                    rs = ps.executeQuery(); 
                    
                    //clears all appointments
                    allContactAppointments.clear();
                               
                    //retrieves appointment info
                    while(rs.next())
                    {
                        int appointmentID = rs.getInt("Appointment_ID");
                        String Title = rs.getString("Title");
                        String Description = rs.getString("Description");
                        String Location = rs.getString("Location");
                        String Type = rs.getString("Type");   
                
                        Timestamp startTime = rs.getTimestamp("Start");             
                        Timestamp endTime = rs.getTimestamp("End");
                        Timestamp createDate = rs.getTimestamp("Create_Date");                
                        String createdBy = rs.getString("Created_By");
                        Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                        String lastUpdatedBy = rs.getString("Last_Updated_By");
                        customerID = rs.getInt("Customer_ID");
                        int userID = rs.getInt("User_ID");
                        contactID = rs.getInt("Contact_ID");
                        formatContact();
                        appointment = new Appointments(appointmentID, Title, Description, Location, Type, convertToLocal(startTime), 
                                convertToLocal(endTime), convertToLocal(createDate), createdBy, convertToLocal(lastUpdate), lastUpdatedBy, 
                                customerID, userID, contactID, contactName);
                        allContactAppointments.add(appointment);
                    }
                    appointmentTableView.setItems(allContactAppointments);
                    appointmentTableView.refresh();
                } 
                catch (SQLException e)
                {
                    e.printStackTrace();
                }           
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }     
            
        }
    }
    
    /**
     * converts a timestamp to local based on user's location and converts to a formatted string
     * @param time
     * @return String
     */
    private String convertToLocal(Timestamp time)
    {
        ZonedDateTime zdtOut = time.toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtOutToLocalTZ = zdtOut.withZoneSameInstant(ZoneId.of(ZoneId.systemDefault().toString()));
        LocalDateTime ldtOutFinal = zdtOutToLocalTZ.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = ldtOutFinal.format(formatter); 
        
        return formatDateTime;   
    }
}
