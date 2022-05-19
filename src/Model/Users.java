/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;


/*
* @author Shawn Ruby
*/

public class Users 
{
    private int UserID;
    private String UserName;
    private String Password;
    private String createDate;
    private String createdBy;
    private String lastUpdate;
    private String lastUpdatedBy;
    
    public Users(int UserID, String UserName, String Password, String createDate, String createdBy, String lastUpdate, String lastUpdatedBy)
    {
        setUserID(UserID);
        setUserName(UserName);
        setPassword(Password);
        setCreateDate(createDate);
        setCreatedBy(createdBy);
        setLastUpdate(lastUpdate);
        setLastUpdatedBy(lastUpdatedBy);
    }
    
    public void setUserID(int UserID)
    {
        this.UserID = UserID;
    }
    
    public void setUserName(String UserName)
    {
        this.UserName = UserName;
    }
    
    public void setPassword(String Password)
    {
        this.Password = Password;
    }
    
    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }
    
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }
    
    public void setLastUpdate(String lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }
    
    public void setLastUpdatedBy(String lastUpdatedBy)
    {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public int getUserID()
    {
        return UserID;
    }
    
    public String getUserName()
    {
        return UserName;
    }
    
    public String getPassword()
    {
        return Password;
    }
    
    public String getCreateDate(){
        return createDate;
    }
    
    public String getCreatedBy(){
        return createdBy;
    }
    
    public String getLastUpdate(){
        return lastUpdate;
    }
    
    public String getLastUpdatedBy(){
        return lastUpdatedBy;
    }
}
