package uis.iot.admin.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatsDetail {
    
    private Long totalDevices = 0L;
    private Long totalModels= 0L;
    private Long totalApps= 0L;

}
