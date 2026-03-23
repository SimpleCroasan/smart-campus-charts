package uis.iot.admin.dto.requests;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.ModelProperty;

@Data
@NoArgsConstructor
public class ModelPropertyForm {

    @Nonnull
    private String name;
    @Nonnull
    private String value;

    //get entity
    public ModelProperty getEntity() {
        ModelProperty modelProperty = new ModelProperty();
        setEntity(modelProperty);
        return modelProperty;
    }

    //set entity
    public void setEntity(ModelProperty modelProperty) {
        modelProperty.setName(name);
        modelProperty.setValue(value);      
    }    
}
