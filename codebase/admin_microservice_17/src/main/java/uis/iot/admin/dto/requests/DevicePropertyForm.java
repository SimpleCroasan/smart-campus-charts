package uis.iot.admin.dto.requests;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.DeviceProperty;

@Data
@NoArgsConstructor
public class DevicePropertyForm {

    @Nonnull
    private String name;
    @Nonnull
    private String value;

    public DeviceProperty getEntity() {
        DeviceProperty deviceProperty = new DeviceProperty();
        setEntity(deviceProperty);
        return deviceProperty;
    }

    public void setEntity(DeviceProperty deviceProperty) {
        deviceProperty.setName(name);
        deviceProperty.setValue(value);
    }    
    
}
