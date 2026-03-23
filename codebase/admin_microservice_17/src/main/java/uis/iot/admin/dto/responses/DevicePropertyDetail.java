package uis.iot.admin.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.DeviceProperty;

@Data
@NoArgsConstructor
public class DevicePropertyDetail {

    private Long devicePropertyId;
    private String name;
    private String value;
    
    public void setEntity(DeviceProperty deviceProperty) {
        this.devicePropertyId = deviceProperty.getDevicePropertyId();
        this.name = deviceProperty.getName();
        this.value = deviceProperty.getValue();
    }
    
}
