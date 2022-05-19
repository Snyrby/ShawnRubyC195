/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Users;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import utils.DBConnection;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/*
* FXML Controller class
* @author Shawn Ruby
*/

public class LoginScreenController implements Initializable 
{
    @FXML private TextField usernameTextBox;
    @FXML private PasswordField passwordTextBox;
    @FXML private Label locationLabel;
    @FXML private Label StartText;
    @FXML private Label locationTextLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    ObservableList<Users> allUsers = FXCollections.observableArrayList();
    String customerName;
    private static Users currentUser;
    private Users user;
    private String logInUserName;
    ResourceBundle rb = ResourceBundle.getBundle("Properties/login");
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //sets the start index as null 
        Platform.runLater(new Runnable() {
        @Override
        public void run() {
            StartText.requestFocus();
        }
        });
        //calls function to get all users
        getAllUsers();
        
        //sets the time zone to the user's timezone and displays it
        ZoneId zoneID = ZoneId.systemDefault();
        locationLabel.setText(zoneID.toString());
        
        //opens a text file to track login activity
        try 
        {
            File myObj = new File("login_activity.txt");
            if (myObj.createNewFile()) 
            {
                System.out.println("File created: " + myObj.getName());
            } 
            else 
            {
            }
        }       
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try 
        {     
            //Locale.setDefault(new Locale("fr"));
            //sets the proper translation based on user location and applies it to the elements on the page
            rb = ResourceBundle.getBundle("Properties/login", Locale.getDefault());   
            locationTextLabel.setText(rb.getString("location"));
            StartText.setText(rb.getString("title"));
            usernameLabel.setText(rb.getString("username"));
            usernameTextBox.setPromptText(rb.getString("usernameprompt"));
            passwordLabel.setText(rb.getString("password"));
            passwordTextBox.setPromptText(rb.getString("passwordprompt"));
            loginButton.setText(rb.getString("title"));
            exitButton.setText(rb.getString("exit"));
        } 
        catch (MissingResourceException e) 
        {
            System.out.println("Missing resource");
        }
    }    
    
    /**
     * Function that triggers if the login button is pushed
     * @param event
     * @throws SQLException
     * @throws IOException 
     */
    
    public void LoginButtonPushed(ActionEvent event) throws SQLException,IOException
    {
        //sets the 3 error labels to hidden
        errorLabel.setVisible(false);
        usernameErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        
        //sets the translation
        rb = ResourceBundle.getBundle("Properties/login", Locale.getDefault());
        
        //sets username and password based on user input
        logInUserName = usernameTextBox.getText();
        String logInPassword = passwordTextBox.getText();
        
        //checks if username and password are valid
        if(validPassword(logInUserName, logInPassword))
        {
            //sets a date and adds a message to the log file with the time and user that successfully logged in
            try 
            {
                Date now = new Date();
                TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()));             
                PrintWriter logWriter = new PrintWriter(new FileWriter("login_activity.txt", true));
                logWriter.println(now + " User: '" + logInUserName + "' has successfully logged in.");
                logWriter.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
            
            //once username and password have been verified then it will compare the user data to the username and set it as a user
            //for sql statement
            getUserName();
            user = returnUserName();
            try
            {
                boolean inFifteenBoolean = false;
                //sql statement that retrives the start time of all appointments for the user that just logged in
                String sql = "SELECT Start, Customer_ID FROM appointments WHERE User_ID = '" + user.getUserID() + "';";
                PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery(); 
                               
                //loops through every start time
                while(rs.next())
                {
                    Timestamp dateCreated = rs.getTimestamp("Start"); 
                    int customerID = rs.getInt("Customer_ID");
                    Date appointmentStartDate = new Date(Timestamp.valueOf(convertToLocal(dateCreated)).getTime());
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");  
                    Date date = new Date();  
                    
                    //converts to milliseconds for comparison
                    Long time = Timestamp.valueOf(convertToLocal(dateCreated)).getTime();
                    
                    //gets 15 minutes in milliseconds
                    int fifteenMinutes = 15 * 60 * 1000;
                    
                    //determines the milliseconds of the user's time plus 15 minutes
                    long inFifteen = System.currentTimeMillis() + fifteenMinutes;
                    
                    //compares current time to current time + 15 minutes or less and compare dates
                    if (inFifteen - time <= fifteenMinutes && dateFormatter.format(date).equals(dateFormatter.format(appointmentStartDate))) 
                    {
                        inFifteenBoolean = true;
                        try
                        {
                            //sql statement that retrives the start time of all appointments for the user that just logged in
                            sql = "SELECT Customer_Name FROM customers WHERE Customer_ID = '" + customerID + "';";
                            ps = DBConnection.getConnection().prepareStatement(sql);
                            rs = ps.executeQuery(); 
                        
                            //loops through every start time
                            while(rs.next())
                            {
                                customerName = rs.getString("Customer_Name");
                            }
                            //subtracts the start time from current time and displays the user has an appointment in a certin minutes
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(time - System.currentTimeMillis());
                            minutes += 1;
                            infoBox("You have an appointment in " + minutes + " minutes with " + customerName + ".", "Information");
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                
                //if the user does not have any appointments then displays no appointments
                if(inFifteenBoolean == false)
                {
                    infoBox("You do not have any appointments within 15 minutes", "Information");
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            
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
        
        //if user and password are blank
        else if(logInUserName.isBlank() && logInPassword.isBlank())
        {            
            usernameErrorLabel.setText(rb.getString("usernameEmpty"));
            passwordErrorLabel.setText(rb.getString("passwordEmpty"));
            usernameErrorLabel.setVisible(true);
            passwordErrorLabel.setVisible(true);
            usernameTextBox.requestFocus();
            usernameTextBox.setText("");
            passwordTextBox.setText("");
        }     
        //if username is blank
        else if(logInUserName.isBlank())
        {
            usernameErrorLabel.setText(rb.getString("usernameEmpty"));
            usernameErrorLabel.setVisible(true);
            usernameTextBox.requestFocus();
            usernameTextBox.setText("");
        }
        //if password is blank
        else if(logInPassword.isBlank())
        {
            passwordErrorLabel.setText(rb.getString("passwordEmpty"));
            passwordErrorLabel.setVisible(true);
            passwordTextBox.requestFocus();
            passwordTextBox.setText("");
        }
        else
        {
            //if the login attempt was incorrect it writes to the log with time
            try 
                {
                    Date now = new Date();
                    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()));
                    PrintWriter logWriter = new PrintWriter(new FileWriter("login_activity.txt", true));
                    logWriter.println(now + " Invalid log in attempt.");
                    logWriter.close();
                    errorLabel.setText(rb.getString("incorrect"));
                    errorLabel.setVisible(true);
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }   
        }        
    }
    
    /**
     * uses sql statement to retrieve all user info
     */
    
    private void getAllUsers()
    {
        try
        {
            //sql statement that retrives all users
            String sql = "SELECT * FROM users;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            //loops through and rerivees all users and adds to an observable list
            while(rs.next())
            {
               int userID = rs.getInt("User_ID");
               String Username = rs.getString("User_Name");
               String Password = rs.getString("Password");
               Timestamp createDate = rs.getTimestamp("Create_Date");
               String createdBy = rs.getString("Created_By");
               Timestamp lastUpdate = rs.getTimestamp("Last_Update");
               String lastUpdatedBy = rs.getString("Last_Updated_By");
               user = new Users(userID, Username, Password, convertToEST(createDate), createdBy, convertToEST(lastUpdate), lastUpdatedBy);
               allUsers.add(user);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * validates password
     * @param logInUserName
     * @param logInPassword
     * @return
     * @throws SQLException 
     */
    
    private boolean validPassword(String logInUserName, String logInPassword) throws SQLException
    {
        //retrieves user input and compares the inputed password to all passwords and compares
        String sqlPassword = "SELECT Password FROM users WHERE User_Name ='" + logInUserName + "'";
        PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlPassword);
        ResultSet passwordResult = ps.executeQuery();
        
        while(passwordResult.next())
        {
            if(passwordResult.getString("Password").equals(logInPassword))
            {
                return true;
            }
        }
        return false;
    }   
    
    /**
     * infobox function
     * @param infoMessage
     * @param titleBar 
     */
    
    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * gets the username based on the person that's logged in
     */
    private void getUserName()
    {
        for(Users selectedUser : allUsers)
        {
            if(selectedUser.getUserName().equals(logInUserName))
            {
                currentUser = selectedUser;
            }
        }
    }
    
    /**
     * retrieves current user info that has logged in
     * @return currentUser
     */
    public static Users returnUserName()
    {
      return currentUser;
    }
    
    public void exitButtonPushed(ActionEvent event) throws IOException
    {    
        System.exit(0);
    }
    
    /**
     * converts a timestamp to local timezone and converts to a formatted string
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
}
