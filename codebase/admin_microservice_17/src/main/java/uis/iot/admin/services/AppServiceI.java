package uis.iot.admin.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uis.iot.admin.dto.requests.AppForm;
import uis.iot.admin.dto.responses.AppDetail;
import uis.iot.admin.models.App;
import uis.iot.admin.models.Device;
import uis.iot.admin.repositories.AppRepository;
import uis.iot.admin.repositories.DeviceRepository;

@Service
public class AppServiceI implements AppService {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private DeviceRepository deviceRepository;


    @Override
    public List<AppDetail> getAppListByUserId(Long userId) {
        Iterable<App> apps = appRepository.findByUserId(userId);
        List<AppDetail> appList = new ArrayList<>();

        apps.forEach(app -> {
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(app);
            appList.add(appDetail);
        });

        return appList;
    }

    @Override
    public AppDetail getAppDetail(Long appId) {
        App app = appRepository.findById(appId).orElse(null);
        if(app != null){
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(app);
            return appDetail;
        }
        return null;        
    }

    @Override
    public AppDetail createApp(AppForm appForm) {
        App app = appForm.getEntity();
        app = appRepository.save(app);
        AppDetail appDetail = new AppDetail();
        appDetail.setEntity(app);
        return appDetail;
    }

    @Override
    public AppDetail updateApp(Long appId,AppForm appForm) {
        App app = appRepository.findById(appId).orElse(null);
        if(app != null){
            appForm.setEntity(app);
            app = appRepository.save(app);
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(appRepository.save(app));
            return appDetail;
        }
        return null;
    }

    @Override
    public void deleteApp(Long appId) {
        appRepository.deleteById(appId);
    }
    
    @Override
    public AppDetail addDeviceApp(Long appId,Long deviceId) {
        App app = appRepository.findById(appId).orElse(null);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if(app != null && device != null){
            if(!app.getAppDevices().contains(device)){
                app.getAppDevices().add(device);
                app = appRepository.save(app);
                AppDetail appDetail = new AppDetail();
                appDetail.setEntity(app);
                return appDetail;
            }            
        }
        return null;
    }

    @Override
    public AppDetail removeDeviceApp(Long appId,Long deviceId) {
        App app = appRepository.findById(appId).orElse(null);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if(app != null && device != null){
            app.getAppDevices().remove(device);
            app = appRepository.save(app);
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(app);
            return appDetail;
        }
        return null;
    }
    
    
}
