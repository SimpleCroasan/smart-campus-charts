package uis.iot.admin.dto.responses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.Device;
import uis.iot.admin.models.DeviceModel;

@Data
@NoArgsConstructor
public class DeviceDetail {

    private Long deviceId;
    private String deviceName;
    private Date creationDate;
    private DeviceModelDetail model;
    private List<DevicePropertyDetail> deviceProperties = new ArrayList<>();
    
    public void setEntity(Device device) {
        this.deviceId = device.getDeviceId();
        this.deviceName = device.getDeviceName();
        this.creationDate = device.getCreationDate();
        this.model = new DeviceModelDetail();

        DeviceModel model = device.getDeviceModel();
        if(model != null){
            this.model.setEntity(model);
        }
        
        if(device.getDeviceProperties() != null){
            device.getDeviceProperties().forEach(property -> {
                DevicePropertyDetail propertyDetail = new DevicePropertyDetail();
                propertyDetail.setDevicePropertyId(property.getDevicePropertyId());
                propertyDetail.setName(property.getName());
                propertyDetail.setValue(property.getValue());
                deviceProperties.add(propertyDetail);
            });
        }
    }

}
