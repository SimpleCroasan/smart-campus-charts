package uis.iot.admin.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.ModelProperty;

@Data
@NoArgsConstructor
public class ModelPropertyDetail {

    private Long modelPropertyId;
    private String name;
    private String value;
    
    public void setEntity(ModelProperty modelProperty) {
        this.modelPropertyId = modelProperty.getModelPropertyId();
        this.name = modelProperty.getName();
        this.value = modelProperty.getValue();
    }
    
}
