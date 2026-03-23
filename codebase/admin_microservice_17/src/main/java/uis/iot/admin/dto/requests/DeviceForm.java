package uis.iot.admin.dto.requests;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.Device;
import uis.iot.admin.models.DeviceModel;
import uis.iot.admin.models.User;

@Data
@NoArgsConstructor
public class DeviceForm {

    @Nonnull
    private String deviceName;

    @Nonnull
    private Long modelId;

    @Nonnull
    private Long userId;

    public Device getEntity(){
        Device device = new Device();
        setEntity(device);
        return device;
    }


    public void setEntity(Device device){
        if(deviceName != null){
            device.setDeviceName(deviceName);
        }
        
        if(modelId != null){
            setModel(device);
        }

        if(userId != null){
            setUser(device);
        }
    }

    public void setModel(Device device){
        DeviceModel model = new DeviceModel();
        model.setModelId(modelId);
        device.setDeviceModel(model);
    }

    public void setUser(Device device){
        User user = new User();
        user.setId(userId);
        device.setUser(user);
    }
    
}
