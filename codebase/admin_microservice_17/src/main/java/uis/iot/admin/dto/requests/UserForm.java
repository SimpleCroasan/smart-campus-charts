package uis.iot.admin.dto.requests;


import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.User;

@Data
@NoArgsConstructor
public class UserForm {

    @Nonnull
    private String userName;

    @Nonnull
    private String password;

    private String email;

    public User getEntity(){
        User user = new User();
        setEntity(user);
        return user;
    } 

    public void setEntity(User user){
        user.setUserName(userName);
        user.setPassword(password);
        user.setEmail(email);
    }

       
}
