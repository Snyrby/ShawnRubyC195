/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shawnrubyc195;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DBConnection;

/**
 * @author Shawn Ruby
 */
public class ShawnRubyC195 extends Application
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        DBConnection.startConnection();
        launch(args);
        DBConnection.closeConnection();
    }

    /**
     * starting function that loads program
     * @param stage
     * @throws Exception 
     */
    @Override
    public void start(Stage stage) throws Exception 
    {
       Parent root = FXMLLoader.load(getClass().getResource("/View/LoginScreen.fxml"));
       Scene scene = new Scene(root);
       stage.setScene(scene);
       stage.setTitle("Software 2");
       stage.setResizable(false);
       stage.show();
    }
}
