/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import static Controller.ViewAppointmentsController.allMonthNames;
import Model.Contacts;
import Model.Location;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import utils.DBConnection;

/**
 * @author Shawn Ruby
 */
public class ViewReportsController implements Initializable 
{
    @FXML private TableView<Location> locationTableView;
    @FXML private TableView<Contacts> contactTableView;
    @FXML private TableColumn<Location, String> locationColumn;
    @FXML private TableColumn<Location, Integer> locationTotalColumn;
    @FXML private TableColumn<Location, String> customerNameLocationColumn;
    @FXML private TableColumn<Contacts, String> contactNameColumn;
    @FXML private TableColumn<Contacts, Integer> appointmentIDColumn;
    @FXML private TableColumn<Contacts, String> titleColumn;
    @FXML private TableColumn<Contacts, String> typeContactColumn;
    @FXML private TableColumn<Contacts, String> descriptionColumn;
    @FXML private TableColumn<Contacts, String> startColumn;
    @FXML private TableColumn<Contacts, String> endColumn;
    @FXML private TableColumn<Contacts, Integer> customerIDColumn;
    
    private ObservableList<Location> allLocations = FXCollections.observableArrayList();
    private ObservableList<Contacts> allContacts = FXCollections.observableArrayList();
    
    @FXML private ComboBox<String> contactComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Label reportLabel;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //sets table columns      
        locationColumn.setCellValueFactory(new PropertyValueFactory<Location, String>("Location"));
        locationTotalColumn.setCellValueFactory(new PropertyValueFactory<Location, Integer>("locationTotal"));
        customerNameLocationColumn.setCellValueFactory(new PropertyValueFactory<Location, String>("customerName"));
        contactNameColumn.setCellValueFactory(new PropertyValueFactory<Contacts, String>("contactName"));
        appointmentIDColumn.setCellValueFactory(new PropertyValueFactory<Contacts, Integer>("appointmentID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Contacts, String>("Title"));
        typeContactColumn.setCellValueFactory(new PropertyValueFactory<Contacts, String>("Type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<Contacts, String>("Description"));
        startColumn.setCellValueFactory(new PropertyValueFactory<Contacts, String>("Start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<Contacts, String>("End"));
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<Contacts, Integer>("customerID"));
        
        fillContactComboBox();
        getAllReports();
        generateTables();
        showContactAppointments();
        fillMonthandTypeComboBox();
        
        //sets a listener on the contact combo box to display their appointment info
        contactComboBox.setOnAction((event) -> {      
        showContactAppointments();
        });
        
        //sets a listener on the type combo box
        typeComboBox.setOnAction((event) -> {      
        if(!monthComboBox.getSelectionModel().isEmpty())
        {
            displayMonthandTypeReport();
        }
        });
        
        //sets a listener on the month combo box
        monthComboBox.setOnAction((event) -> {      
        if(!typeComboBox.getSelectionModel().isEmpty())
        {
            displayMonthandTypeReport();
        }
        });
    }
    
    /**
     * sql statement used to retrieve all report info and saves as an object
     */
    private void getAllReports()
    {
        try
        {
            //retreives customer name and customer ID from sql statement
            String sql = "SELECT Customer_Name, Customer_ID FROM customers;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
                    
            while(rs.next())
            {
                int Customer_ID = rs.getInt("Customer_ID");
                String customerName = rs.getString("Customer_Name");
                
                try
                {
                    //retreives start time, type and location from sql statement
                    String sql2 = "SELECT Location FROM appointments WHERE Customer_ID = '" + Customer_ID +"';";
                    PreparedStatement ps2 = DBConnection.getConnection().prepareStatement(sql2);
                    ResultSet rs2 = ps2.executeQuery();            

                    while(rs2.next())
                    {

                        //boolean statement to check if an object already exists
                        Boolean alreadyExist = false;

                        //retrieves info from sql statement
                        String Location = rs2.getString("Location");

                        //works exactly the same as month
                        alreadyExist = false;
                        if(allLocations.isEmpty())
                        {
                            Location location = new Location(customerName, Location, 1);
                            allLocations.add(location);
                        }
                        else
                        {
                            for(Location location : allLocations)
                            {
                                if(location.getLocation().equalsIgnoreCase(Location) && location.getCustomerName().equals(customerName))
                                {
                                    int totalLocation = location.getLocationTotal() + 1;
                                    location.setLocationTotal(totalLocation);
                                    alreadyExist = true;
                                    break;
                                }
                            }
                            if(alreadyExist == false)
                            {
                                Location location = new Location(customerName, Location, 1);
                                allLocations.add(location);
                            }
                        }        
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * generate tables to display all report data
     */
    private void generateTables() 
    {
        locationTableView.setItems(allLocations);
        locationTableView.refresh();
        contactTableView.setItems(allContacts);
        contactTableView.refresh();
    }
    
    /**
     * reverts back to previous appointment screen
     * @param event
     * @throws IOException 
     */
    public void returnButtonPushed(ActionEvent event) throws IOException
    {    
        //loads the customer main screen
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
     * runs sql command to retrieve all contact names and adds to a combo box
     */
    private void fillContactComboBox()
    {
        try
        {
            //retreives contact name
            String sql = "SELECT Contact_Name FROM contacts;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
                    
            while(rs.next())
            {
                String contactName = rs.getString("Contact_Name");
                contactComboBox.getItems().add(contactName);
            }
            contactComboBox.getSelectionModel().selectFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * listener that updates the contact table
     */
    private void showContactAppointments() 
    {
        try
        {
            allContacts.clear();
            
            //retreives customer name and customer ID from sql statement
            String contactName = contactComboBox.getSelectionModel().getSelectedItem();
            String sql = "SELECT Contact_ID FROM contacts WHERE Contact_Name = '" + contactName + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
                    
            while(rs.next())
            {
                int ContactID = rs.getInt("Contact_ID");
                try
                {
                    //retreives all appointment info from each contact from sql statement
                    String sql2 = "SELECT Appointment_ID, Title, Description, Type, Start, End, Customer_ID FROM appointments WHERE Contact_ID = '" + ContactID +"';";
                    PreparedStatement ps2 = DBConnection.getConnection().prepareStatement(sql2);
                    ResultSet rs2 = ps2.executeQuery();            

                    while(rs2.next())
                    {
                        int appointmentID = rs2.getInt("Appointment_ID");
                        String Title = rs2.getString("Title");
                        String Description = rs2.getString("Description");
                        String Type = rs2.getString("Type");
                        Timestamp Start = rs2.getTimestamp("Start");
                        Timestamp End = rs2.getTimestamp("End");
                        int customerID = rs2.getInt("Customer_ID");
                        Contacts contact = new Contacts(contactName, appointmentID, Title, Type, Description, 
                                convertToEST(Start), convertToEST(End), customerID);
                        allContacts.add(contact);
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * retrieves the count of how many appointments exist that match the criteria of what's selected in type and month and displays the value
     */
    private void displayMonthandTypeReport() 
    {
        try
        {
            //retreives contact name
            String sql = "SELECT count(*) FROM appointments WHERE Type = '" + typeComboBox.getSelectionModel().getSelectedItem() +
                    "' AND monthname(Start) = '" + monthComboBox.getSelectionModel().getSelectedItem() + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
                    
            while(rs.next())
            {
                int reportCount = rs.getInt(1);
                reportLabel.setVisible(true);
                reportLabel.setText("There are " + reportCount + " that match this criteria.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * fills the type combobox with all the distinct types
     */
    private void fillMonthandTypeComboBox() 
    {
        monthComboBox.getItems().addAll(allMonthNames);
        try
        {
            //retreives all types
            String sql = "SELECT distinct Type FROM appointments;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
                    
            while(rs.next())
            {
                typeComboBox.getItems().add(rs.getString("Type"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}

    
