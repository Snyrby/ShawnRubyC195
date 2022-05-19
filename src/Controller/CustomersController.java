/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Countries;
import Model.Customer;
import Model.FirstLevelDivision;
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
import javafx.fxml.Initializable;
import utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/*
* @author Shawn Ruby
*/

public class CustomersController implements Initializable 
{
    Customer customer;
    Countries country;
    FirstLevelDivision state;
    private static Customer selectedCustomer;
    
    @FXML private TableView<Customer> customerTableView;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, String> zipCodeColumn;
    @FXML private TableColumn<Customer, String> phoneNumberColumn;
    @FXML private TableColumn<Customer, String> stateColumn;
    @FXML private TableColumn<Customer, String> countryColumn;
    
    private int divisionID;
    private String division;
    private String Country;
    
    private ObservableList<Customer> allCustomer = FXCollections.observableArrayList();
    private static ObservableList<Countries> allCountries = FXCollections.observableArrayList();
    private static ObservableList<FirstLevelDivision> allState = FXCollections.observableArrayList();
    
    /**
    * Initializes the controller class.
    * @param url
    * @param rb 
    */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //sets the columns up for tableview
        nameColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("CustomerName"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("Address"));
        zipCodeColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("ZipCode"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("PhoneNumber"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("State"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("Country"));
        
        //calls function to get all customers and format the table view
        getAllCustomers();
        generateCustomerTable();  
        
        //gets all countries and all states
        if(allCountries.isEmpty())
        {
            getAllCountries();
        }
        if(allState.isEmpty())
        {
            getAllFirstLevelDivision();
        }
        
    }    
    
    
    /**
     * function that retrieves all customer data from sql statement
     */
    private void getAllCustomers()
    {
        try
        {
            //sql statement that selects all customers
            String sql = "SELECT * FROM customers;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            
            //clears the customers if there's any residual
            allCustomer.clear();    
            
            //retrieves all data from the sql querie and adds it to a customer object
            while(rs.next())
            {
                int customerID = rs.getInt("Customer_ID");
                String customerName = rs.getString("Customer_Name");
                String Address = rs.getString("Address");
                String ZipCode = rs.getString("Postal_Code");
                String PhoneNumber = rs.getString("Phone");
                Timestamp createDate = rs.getTimestamp("Create_Date");        
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                divisionID = rs.getInt("Division_ID");
                formatCountry();
                customer = new Customer(customerID, customerName, Address, ZipCode, PhoneNumber, convertToLocal(createDate), 
                        createdBy, convertToLocal(lastUpdate), lastUpdatedBy, divisionID, division, Country);
                allCustomer.add(customer);
            }
        } 
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    
    /**
     * sets the table view to customer data
     */
    private void generateCustomerTable()
    {
        customerTableView.setItems(allCustomer);
        customerTableView.refresh();
    }
    
    
    /**
     * when the add customer button pushed it triggers this function
     * @param event
     * @throws IOException 
     */
    public void customerAddButtonPushed(ActionEvent event) throws IOException
    {
        //loads the add customer page
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/AddCustomer.fxml"));
        Parent tableViewParent = loader.load();
        Scene tableViewScene = new Scene(tableViewParent);  
            
        //This line gets the Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(tableViewScene);
        window.setResizable(false);
        window.setTitle("Add Customer");
        window.show();
    }
    
    
    /**
     * when the user presses the modify customer button, this function is triggered
     * @param event
     * @throws IOException 
     */
    public void modifyCustomerButtonPushed(ActionEvent event) throws IOException
    {
        //sets the selected customer to be modified as what the user selects
        selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        
        //if null then it will throw an error
        if (selectedCustomer == null)
        {
            Alert nullalert = new Alert(Alert.AlertType.ERROR);
            nullalert.setTitle("Customer Modify Error");
            nullalert.setHeaderText("Can't Modify Customer");
            nullalert.setContentText("There was no Customer selected!");
            nullalert.showAndWait();
        }
        
        //if the user has selected a customer, then it will open the modify customer screen
        else
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/View/modifyCustomer.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent); 
            
            //passes the customer data to the modify customer screen
            ModifyCustomerController controller = loader.getController();
            controller.initData(selectedCustomer);
            
            //This line gets the Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.setResizable(false);
            window.setTitle("Modify Customer");
            window.show();
        }
        
    }

    
    /**
     * runs sql statement and adds all country info to country object
     */
    private void getAllCountries()
    {
        try
        {          
            String sql = "SELECT * FROM countries;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                int countryID = rs.getInt("Country_ID");
                Country = rs.getString("Country");
                Timestamp createDate = rs.getTimestamp("Create_Date");
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                country = new Countries(countryID, Country, convertToLocal(createDate), createdBy, convertToLocal(lastUpdate), lastUpdatedBy);
                allCountries.add(country);
            }
        } 
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * runs sql statement and adds all state info into a state object
     */
    private void getAllFirstLevelDivision() 
    {
        try
        {          
            String sql = "SELECT * FROM first_level_divisions;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                divisionID = rs.getInt("Division_ID");
                division = rs.getString("Division");
                Timestamp createDate = rs.getTimestamp("Create_Date");
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                int countryID = rs.getInt("COUNTRY_ID");
                state = new FirstLevelDivision(divisionID, division, convertToLocal(createDate), createdBy, convertToLocal(lastUpdate), 
                        lastUpdatedBy, countryID);
                allState.add(state);
            }
        } 
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * retrieves the country from the division id to be stored for convenience
     */
    private void formatCountry() 
    {
        try
        {          
            String sql = "SELECT Division, COUNTRY_ID FROM first_level_divisions WHERE Division_ID = '" + divisionID + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                division = rs.getString("Division");
                int countryID = rs.getInt("COUNTRY_ID");
                try
                {          
                    String sqlTwo = "SELECT Country FROM countries WHERE COUNTRY_ID = " + countryID + ";";
                    PreparedStatement psTwo = DBConnection.getConnection().prepareStatement(sqlTwo);
                    ResultSet rsTwo = psTwo.executeQuery(); 
                    while(rsTwo.next())
                    {
                        Country = rsTwo.getString("Country");
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
     * runs this function if the appointment button is pushed
     * @param event
     * @throws IOException 
     */
    public void appointmentButtonPushed(ActionEvent event) throws IOException
    {
        selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        
        //if the user's selected customer is null then throws error
        if (selectedCustomer == null)
        {
            Alert nullalert = new Alert(Alert.AlertType.ERROR);
            nullalert.setTitle("Customer View Error");
            nullalert.setHeaderText("Can't View Customer Appointment Info");
            nullalert.setContentText("There was no Customer selected!");
            nullalert.showAndWait();
        }
        
        //if not null then opens the appointment page
        else
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/View/ViewAppointments.fxml"));
            Parent tableViewParent = loader.load();
            Scene tableViewScene = new Scene(tableViewParent);  
            

            //This line gets the Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(tableViewScene);
            window.setResizable(false);
            window.setTitle("Appointment Info");
            window.show();
        }
    }
    
    /**
     * function that runs if user presses delete customer button
     */
    public void deleteCustomerButtonPushed()
    {
        //gets user's selection
        selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        
        //if selection is null then displays error
        if (selectedCustomer == null)
        {
            infoBox("PLEASE SELECT AN CUSTOMER TO DELETE!", "ERROR");
        }
        //if user does select someone..
        else
        {
            try
            {
                //sql statement used to see if the customer has any appointments
                String sql = "SELECT Appointment_ID FROM appointments WHERE Customer_ID = '" + selectedCustomer.getCustomerID()+ "';";
                PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                
                //if there is no appointments
                if(!rs.isBeforeFirst())
                {
                    try
                    {
                        //dialog box that asks the user for confirmation
                        int input = JOptionPane.showConfirmDialog(null, "Do you want to Delete the appointment for " + selectedCustomer.getCustomerName() + "?");
                        if(input == 0)
                        {
                            //if yes then runs sql statement to remove customer and remove from observable list
                            sql = "DELETE FROM customers WHERE Customer_ID = '" + selectedCustomer.getCustomerID()+ "';";
                            ps = DBConnection.getConnection().prepareStatement(sql);
                            ps.executeUpdate();
                            allCustomer.remove(selectedCustomer);
                            generateCustomerTable();
                        }          
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
                //if customer has appointments then runs error message
                else
                {
                    infoBox("THAT CUSTOMER HAS APPOINTMENTS - PLEASE DELETE THEM FIRST!", "ERROR");
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }        
    }
    
    /**
     * logout button reverts to previous page
     * @param event
     * @throws IOException 
     */
    public void logoutButtonPushed(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/LoginScreen.fxml"));
        Parent tableViewParent = loader.load();
        Scene tableViewScene = new Scene(tableViewParent);  
            
        //This line gets the Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(tableViewScene);
        window.setResizable(false);
        window.setTitle("Software 2");
        window.show();
    }
    
    /**
     * infobox function
     * @param infoMessage
     * @param titleBar 
     */
    private static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * returns the allState observable list 
     * @return 
     */
    public static ObservableList<FirstLevelDivision> returnStates()
    {
        return allState;
    }
    
    /**
     * returns the countries observable list
     * @return 
     */
    public static ObservableList<Countries> returnCountries()
    {
        return allCountries;
    }
    
    /**
     * returns the selected customer to be used in the modify page
     * @return 
     */
    public static Customer returnSelectedCustomer()
    {
        return selectedCustomer;
    }
    
    /**
     * opens the report screen when button is pushed
     * @param event
     * @throws IOException 
     */
    public void viewReportsButtonPushed(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/ViewReports.fxml"));
        Parent tableViewParent = loader.load();
        Scene tableViewScene = new Scene(tableViewParent);  
            
        //This line gets the Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(tableViewScene);
        window.setResizable(false);
        window.setTitle("View Reports");
        window.show();
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
