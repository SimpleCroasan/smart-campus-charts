package uis.iot.admin.dto.responses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import uis.iot.admin.models.App;

@Data
@NoArgsConstructor
public class AppDetail {

    private Long appId;
    private String appName;
    private Date creationDate;
    private List<DeviceDetail> appDevices = new ArrayList<>();

    public void setEntity(App app){
        this.appId = app.getAppId();
        this.appName = app.getAppName();
        this.creationDate = app.getCreationDate();

        if(app.getAppDevices() != null){
            app.getAppDevices().forEach(device -> {
                DeviceDetail deviceDetail = new DeviceDetail();
                deviceDetail.setEntity(device);
                appDevices.add(deviceDetail);
            });
        }
    }
    
    
}
