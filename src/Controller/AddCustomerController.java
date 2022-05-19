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

/**
* @author Shawn Ruby
*/
public class AddCustomerController implements Initializable 
{

    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField AddressTextField;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private ComboBox<String> stateComboBox;
    @FXML private TextField zipCodeTextField;
    @FXML private TextField phoneNumberTextField;
    
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
    private String comboBoxCountry;
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
        
        //adds a listener for when the user selects a country, it adds the first level division related to the country
        countryComboBox.setOnAction((event) -> {
        selectedItem = countryComboBox.getSelectionModel().getSelectedItem();
        loadStateComboBox();
        });   
    }    
    
    /**
     * runs a for statement and adds all countries to the country combobox
     */
    private void loadCountryComboBox()
    {
        for(Countries currentCountry : returnCountries())
        {
            comboBoxCountry = currentCountry.getCountry();
            countryComboBox.getItems().add(comboBoxCountry);
        }       
    }
    
    /**
     * adds all first level division to the first level division combobox
     */
    private void loadStateComboBox()
    {       
        //clears the combobox
        stateComboBox.getItems().clear();
        try
        {
            //sql statement that retrieves country id from user's selected country
            String sql = "SELECT Country_ID FROM countries WHERE Country = '" + selectedItem +"';";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery(); 
            while(rs.next())
            {
                int countryID = rs.getInt("Country_ID");
                try
                {
                    //sql statement that retrieves the division name that equaled the country id
                    String sqlTwo = "SELECT Division FROM first_level_divisions WHERE COUNTRY_ID = '" + countryID +"';";
                    PreparedStatement psTwo = DBConnection.getConnection().prepareStatement(sqlTwo);
                    ResultSet rsTwo = psTwo.executeQuery(); 
                    while(rsTwo.next())
                    { 
                        //adds the division name to the combo box
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
     * input validation function that returns false if input validation is false
     * @return 
     */
    private boolean inputValidation() 
    {
        //sets all the input validation labels to hidden
        firstNameLabel.setVisible(false);
        lastNameLabel.setVisible(false);
        addressLabel.setVisible(false);
        phoneNumberLabel.setVisible(false);
        zipCodeLabel.setVisible(false);
        countryLabel.setVisible(false);
        stateLabel.setVisible(false);
        
        //sets the valid statement to true (false if input validation is false)
        boolean valid = true;       
        
        //selects first name from user input
        String firstNameInput = firstNameTextField.getText();
        
        //if the input is null then alerts the user and sets focus
        if(firstNameInput.isEmpty())
        {
            valid = false;
            firstNameLabel.setVisible(true);
            firstNameTextField.setText("");
            firstNameTextField.requestFocus();
        }
        else
        {
            //sets the firstname to a char array
            char[] chars = firstNameInput.toCharArray();
            
            //if not null, loop through each letter
            for (char c : chars) 
            {
                //if the character is not a letter or is a space then flags and alerts user
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

        //this section is the same as the firstname but for the last name
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
        
        //creates a full name by added first name + space + last name
        fullName = firstNameInput + " " + lastNameInput;
        
        //validates if the name is less 50 characters to satisfy the database limit. if not, then sets a flag and alerts user
        if(fullName.length() > 50)
        {
            valid = false;
            firstNameLabel.setVisible(true);
            lastNameLabel.setVisible(true);
            firstNameTextField.setText("");
            lastNameTextField.setText("");
            firstNameTextField.requestFocus();
        }
        
        //gets address from user input
        addressInput = AddressTextField.getText();
        
        //checks if address is empty or greater than 100 and if it is then sets a flag and notifies user
        if(addressInput.isEmpty() || addressInput.length() > 100)
        {
            valid = false;
            addressLabel.setVisible(true);
            AddressTextField.setText("");
            AddressTextField.requestFocus();
        }
        
        //gets phone number from user input
        phoneNumberInput = phoneNumberTextField.getText();
        
        //checks if input is blank or not equal to 10 chars. sets flag and alerts user if not
        if(phoneNumberInput.isBlank() || phoneNumberInput.length() != 10)
        {     
            valid = false;
            phoneNumberTextField.setText("");
            phoneNumberTextField.requestFocus();
            phoneNumberLabel.setVisible(true);            
        }
        //if passes validation
        else
        {
            //sends the phone number to a char array
            char[] chars = phoneNumberInput.toCharArray();
        
            //loops throuch each character
            for (char c : chars) 
            {
                //if it is a number then do nothing
                if(Character.isDigit(c)) 
                {
                }
                //if not a number then flag and alert user
                else
                {
                    valid = false;
                    phoneNumberTextField.setText("");
                    phoneNumberTextField.requestFocus();
                    phoneNumberLabel.setVisible(true);
                    break;
                }
            }
            //formats the phone number to be 'xxx-xxx-xxxx'
            phoneNumberInput = phoneNumberInput.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3");
        }
        
        //this is the exact same as phone number but for the zip code
        zipCodeInput = zipCodeTextField.getText();
        
        if(zipCodeInput.isEmpty() || zipCodeInput.length() != 5)
        {
            valid = false;
            zipCodeLabel.setVisible(true);
            zipCodeTextField.requestFocus();
            zipCodeTextField.setText("");
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
     * if the user presses the add customer button, it runs this function
     */
    public void addCustomerButtonPushed()
    {
        //runs the input validation function and if valid = true
        if(inputValidation())
        {
            //creates a timestamp of the user's local time and translates to a time format
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant instant = timestamp.toInstant();
            Timestamp tsFromInstant = Timestamp.from(instant);
            
            //gets the username from log in form of the current user logged in
            Users userName = returnUserName();
            String activeUser = userName.getUserName();
            
            //gets the division id
            getDivisionID();
            try
            {
                //asks user to confirm if they want to add the new customer
                int input = JOptionPane.showConfirmDialog(null, "Do you want to add " + fullName + "?");
                //if user confirms, runs a sql statement to add the new customer to database
                if(input == 0)
                {
                    String sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, "
                        + "Created_By, Last_Update, Last_Updated_By, Division_ID)"
                        + "VALUES ('" + fullName + "', '" + addressInput + "', '" + zipCodeInput + "', '" + phoneNumberInput + "', '" 
                        + convertToUTC(tsFromInstant) + "', '" + activeUser + "', '" + convertToUTC(tsFromInstant) 
                        + "', '" + activeUser + "', '" + divisionID + "');";
                    PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                    ps.executeUpdate();
                    firstNameTextField.setText("");
                    lastNameTextField.setText("");
                    AddressTextField.setText("");
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
     * function that runs a sql statement to get division id based on user input of first division
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
     * cancel button that reverts to the previous page
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
