/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import static Controller.CustomersController.returnCountries;
import static Controller.LoginScreenController.returnUserName;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import Model.Countries;
import Model.Customer;
import Model.Users;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import utils.DBConnection;

/*
 * FXML Controller class
 * @author Shawn Ruby
 */
public class ModifyCustomerController implements Initializable 
{
    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField addressTextField;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private ComboBox<String> stateComboBox;
    @FXML private TextField zipCodeTextField;
    @FXML private TextField phoneNumberTextField;
    @FXML private TextField customerIDTextField;
    
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label addressLabel;
    @FXML private Label phoneNumberLabel;
    @FXML private Label zipCodeLabel;
    @FXML private Label countryLabel;
    @FXML private Label stateLabel;
    
    private Object selectedItem;
    private String fullName;
    private String addressInput;
    private String phoneNumberInput;
    private String zipCodeInput;
    private String division;
    private String Country;
    private int divisionID;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        loadCountryComboBox();
        
        //adds a listener that adds first level division based on user selection of country
        countryComboBox.setOnAction((event) -> {
        selectedItem = countryComboBox.getSelectionModel().getSelectedItem();
        loadStateComboBox();
        });   
    }    
    
    /**
     * runs a sql statement to add all countries to the country combo box
     */
    private void loadCountryComboBox()
    {
        for(Countries currentCountry : returnCountries())
        {
            Country = currentCountry.getCountry();
            countryComboBox.getItems().add(Country);
        }       
    }
    
    /**
     * runs a sql statement to load first level division into the first level division combobox
     */
    private void loadStateComboBox()
    {        
        //clears the first level division combobox
        stateComboBox.getItems().clear();
        try
        {
            //sql statement that selects the first level division based on the user's input of country
            String sql = "SELECT Country_ID FROM countries WHERE Country = '" + selectedItem +"';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                int countryID = rs.getInt("Country_ID");
                try
                {
                    String sqlTwo = "SELECT Division FROM first_level_divisions WHERE COUNTRY_ID = '" + countryID +"';";
                    PreparedStatement psTwo = DBConnection.getConnection().prepareStatement(sqlTwo);
                    ResultSet rsTwo = psTwo.executeQuery(); 
                    while(rsTwo.next())
                    { 
                        division = rsTwo.getString("Division");
                        stateComboBox.getItems().add(division);
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
     * retrieves country name from sql statement to add to object for convenience
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
     * retrieves customer info from customer selected
     * @param selectedCustomer 
     */
    public void initData(Customer selectedCustomer) 
    {
        //rerives selected customer
        Customer modifyCustomer = selectedCustomer;
        
        //sets the customer id textfield to
        customerIDTextField.setText(String.valueOf(modifyCustomer.getCustomerID()));
        
        //retrieves customers name and splits it into firstname and last name then sets the text to the name
        fullName = modifyCustomer.getCustomerName();
        String[] parts = fullName.split(" ");
        String currentFirstName = parts[0];
        String currentLastName = parts[1];
        firstNameTextField.setText(currentFirstName);
        lastNameTextField.setText(currentLastName);
        
        //sets text to customer's address
        addressTextField.setText(modifyCustomer.getAddress());
        
        //sets the combobox values to the customer's country and first level division   
        divisionID = modifyCustomer.getDivisionID();
        formatCountry();
        countryComboBox.setValue(Country);
        stateComboBox.setValue(division);
        
        //sets the text to phonenumber and removes the '-' from the phone number
        phoneNumberInput = modifyCustomer.getPhoneNumber();
        parts = phoneNumberInput.split("-");
        phoneNumberInput = "";
        for (int i = 0; i < parts.length; i++)
        {
            phoneNumberInput += parts[i];
        }
        phoneNumberTextField.setText(phoneNumberInput);
        
        //sets the text for the zip code
        zipCodeTextField.setText(modifyCustomer.getZipCode());
        
        //selected item = what is current in the combo box and then retrieves first level division
        selectedItem = countryComboBox.getValue();
        loadStateComboBox();
    }
    
    /**
     * get division id from user input of first level division
     */
    private void getDivisionID()
    {
        division = stateComboBox.getSelectionModel().getSelectedItem();
        try
        {
            String sql = "SELECT Division_ID FROM first_level_divisions WHERE Division = '" + division + "';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                divisionID = rs.getInt("Division_ID");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * input validation is exactly the same as add customer. see add customer if comments are needed
     * @return 
     */
    private boolean inputValidation() 
    {
        firstNameLabel.setVisible(false);
        lastNameLabel.setVisible(false);
        addressLabel.setVisible(false);
        phoneNumberLabel.setVisible(false);
        zipCodeLabel.setVisible(false);
        countryLabel.setVisible(false);
        stateLabel.setVisible(false);
        boolean valid = true;       
        
        String firstNameInput = firstNameTextField.getText();
        
        if(firstNameInput.isEmpty())
        {
            valid = false;
            firstNameLabel.setVisible(true);
            firstNameTextField.setText("");
            firstNameTextField.requestFocus();
        }
        else
        {
            char[] chars = firstNameInput.toCharArray();
            for (char c : chars) 
            {
                if(!Character.isLetter(c) || Character.isWhitespace(c)) 
                {
                    valid = false;
                    firstNameLabel.setVisible(true);
                    firstNameTextField.setText("");
                    firstNameTextField.requestFocus();
                    break;
                }
            }
        }

        String lastNameInput = lastNameTextField.getText();
        
        if(lastNameInput.isEmpty())
        {
            valid = false;
            lastNameLabel.setVisible(true);
            lastNameTextField.setText("");
            lastNameTextField.requestFocus();
        }
        else
        {
            char[] chars = lastNameInput.toCharArray();
            for (char c : chars) 
            {
                if(!Character.isLetter(c) || Character.isWhitespace(c)) 
                {
                    valid = false;
                    lastNameLabel.setVisible(true);
                    lastNameTextField.setText("");
                    lastNameTextField.requestFocus();
                    break;
                }
            }
        }
        
        fullName = firstNameInput + " " + lastNameInput;
        if(fullName.length() > 50)
        {
            valid = false;
            firstNameLabel.setVisible(true);
            lastNameLabel.setVisible(true);
            firstNameTextField.setText("");
            lastNameTextField.setText("");
            firstNameTextField.requestFocus();
        }
         
        addressInput = addressTextField.getText();
        if(addressInput.isEmpty() || addressInput.length() > 100)
        {
            valid = false;
            addressLabel.setVisible(true);
            addressTextField.setText("");
        }
        
        phoneNumberInput = phoneNumberTextField.getText();
        
        if(phoneNumberInput.isBlank() || phoneNumberInput.length() != 10)
        {     
            valid = false;
            phoneNumberTextField.setText("");
            phoneNumberTextField.requestFocus();
            phoneNumberLabel.setVisible(true);            
        }
        else
        {
            char[] chars = phoneNumberInput.toCharArray();
            for (char c : chars) 
            {
                if(Character.isDigit(c)) 
                {
                }
                else
                {
                    valid = false;
                    phoneNumberTextField.setText("");
                    phoneNumberTextField.requestFocus();
                    phoneNumberLabel.setVisible(true);
                    break;
                }
            }
            phoneNumberInput = phoneNumberInput.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3");
        }
        
        zipCodeInput = zipCodeTextField.getText();
        
        if(zipCodeInput.isEmpty() || zipCodeInput.length() != 5)
        {
            valid = false;
            zipCodeLabel.setVisible(true);
            zipCodeTextField.setText("");
            zipCodeTextField.requestFocus();
        }
        else
        {
            char[] chars = zipCodeInput.toCharArray();
            for (char c : chars) 
            {
                if(!Character.isLetterOrDigit(c)) 
                {
                    valid = false;
                    zipCodeTextField.setText("");
                    zipCodeTextField.requestFocus();
                    zipCodeLabel.setVisible(true);
                    break;
                }
            }
        }
        
        if(countryComboBox.getSelectionModel().isEmpty())
        {
            valid = false;
            countryComboBox.requestFocus();
            countryLabel.setVisible(true);
        }
        
        if(stateComboBox.getSelectionModel().isEmpty())
        {
            valid = false;
            stateComboBox.requestFocus();
            stateLabel.setVisible(true);
        }
        return valid;
    }
    
    /**
     * function for if update customer button is pushed
     */
    public void updateCustomerButtonPushed()
    {
        //confirms input validation
        if(inputValidation())
        {
            //creates user local time
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant instant = timestamp.toInstant();
            Timestamp tsFromInstant = Timestamp.from(instant);
            
            //returns username
            Users userName = returnUserName();
            String activeUser = userName.getUserName();
            
            //gets first division id
            getDivisionID();
            try
            {
                //sql statement to modify the value if the user confirms
                int input = JOptionPane.showConfirmDialog(null, "Do you want to Change " + fullName + "?");
                if(input == 0)
                {
                    String sql = "UPDATE customers SET Customer_Name = '" + fullName + "', Address = '" + addressInput + "', Postal_Code = '" + zipCodeInput + "', Phone = '" + phoneNumberInput
                            + "', Last_Update = '" + convertToUTC(tsFromInstant) + "', Last_Updated_By = '" + activeUser + "', Division_ID = '" + divisionID + "' "
                            + "WHERE Customer_ID = '" + customerIDTextField.getText() + "';";
                    PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                    ps.executeUpdate();
                    firstNameTextField.setText("");
                    lastNameTextField.setText("");
                    addressTextField.setText("");
                    phoneNumberTextField.setText("");
                    zipCodeTextField.setText("");
                    countryComboBox.valueProperty().set("");
                    stateComboBox.valueProperty().set("");
                }          
            }
            catch (SQLException e)
            {
            e.printStackTrace();
            }
        }
    }
    
    /**
     * goes back to the previous page
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
}
