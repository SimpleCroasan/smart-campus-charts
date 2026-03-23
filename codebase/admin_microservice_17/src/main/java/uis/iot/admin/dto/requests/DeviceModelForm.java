package uis.iot.admin.dto.requests;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.DeviceModel;
import uis.iot.admin.models.DeviceType;
import uis.iot.admin.models.User;

@Data
@NoArgsConstructor
public class DeviceModelForm {

    @Nonnull
    private String modelName;

    @Nonnull
    private String type;

    @Nonnull
    private Long userId;

    public DeviceModel getEntity(){
        DeviceModel deviceModel = new DeviceModel();
        setEntity(deviceModel);
        return deviceModel;
    }

    public void setEntity(DeviceModel deviceModel) {
        if(modelName != null){
            deviceModel.setModelName(modelName);
        }
        
        if(type != null){
            deviceModel.setType(DeviceType.valueOf(type));
        }

        if(userId != null){
            assignUser(deviceModel);
        }
                
    }

    private void assignUser(DeviceModel deviceModel){
        User user = new User();
        user.setId(userId);
        deviceModel.setUser(user);
    }  
    
}
