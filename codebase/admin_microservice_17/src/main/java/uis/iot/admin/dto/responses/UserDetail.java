package uis.iot.admin.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.User;

@Data
@NoArgsConstructor
public class UserDetail {

    private Long userId;
    private String userName;
    private String email;
    private String userUniqueCode;
    
    public void setEntity(User user){
        userId = user.getId();
        userName = user.getUserName();
        email = user.getEmail();
        userUniqueCode = user.getUserUniqueCode();
    }
    
}
