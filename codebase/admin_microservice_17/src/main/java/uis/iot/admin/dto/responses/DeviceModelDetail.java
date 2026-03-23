package uis.iot.admin.dto.responses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.DeviceModel;

@Data
@NoArgsConstructor
public class DeviceModelDetail {

    private Long modelId;
    private String modelName;
    private String type;
    private Date creationDate;
    private List<ModelPropertyDetail> modelProperties = new ArrayList<>();

    //set entity
    public void setEntity(DeviceModel deviceModel) {
        this.modelId = deviceModel.getModelId();
        this.modelName = deviceModel.getModelName();
        this.type = deviceModel.getType().toString();
        this.creationDate = deviceModel.getCreationDate();   
        
        if(deviceModel.getDeviceModelProperties() != null){
            deviceModel.getDeviceModelProperties().forEach(property -> {
                ModelPropertyDetail modelProperty = new ModelPropertyDetail();
                modelProperty.setEntity(property);
                modelProperties.add(modelProperty);
            });
        }
    }
    
}
