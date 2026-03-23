package uis.iot.admin.dto.requests;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceAppForm {

    @Nonnull
    private Long appId;

    @Nonnull
    private Long deviceId;
    
}
