package uis.iot.admin.dto.requests;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.App;
import uis.iot.admin.models.User;

@Data
@NoArgsConstructor
public class AppForm {

    @Nonnull
    private String appName;

    @Nonnull
    private Long userId;

    public App getEntity(){
        App app = new App();
        setEntity(app);
        return app;
    }
    
    public void setEntity(App app){
        app.setAppName(appName);
        if(userId != null){
            assignUser(app);
        }
    }

    private void assignUser(App app){
        User user = new User();
        user.setId(userId);
        app.setUser(user);
    }    
    
}
