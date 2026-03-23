package uis.iot.admin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uis.iot.admin.dto.requests.AppForm;
import uis.iot.admin.dto.requests.DeviceAppForm;
import uis.iot.admin.dto.responses.AppDetail;
import uis.iot.admin.services.AppService;

@RestController
@RequestMapping("/api/v1/apps")
public class AppController {

    @Autowired
    private AppService appService;

    @GetMapping("/user/{userId}")
    public List<AppDetail> getAppListByUserId(@PathVariable Long userId) {
        return appService.getAppListByUserId(userId);
    }

    @GetMapping("/{appId}")
    public AppDetail getAppDetail(@PathVariable Long appId) {
        return appService.getAppDetail(appId);
    }

    @PostMapping("/new")
    public AppDetail createApp(@RequestBody AppForm newAppData) {
        return appService.createApp(newAppData);
    }

    @PutMapping("/update/{appId}")
    public AppDetail updateApp(@PathVariable Long appId, @RequestBody AppForm newAppData) {
        return appService.updateApp(appId, newAppData);
    }

    @DeleteMapping("/delete/{appId}")
    public void deleteApp(@PathVariable Long appId) {
        appService.deleteApp(appId);
    }

    @PostMapping("/add/device/")
    public AppDetail addDeviceApp(@RequestBody DeviceAppForm deviceAppForm) {
        return appService.addDeviceApp(deviceAppForm.getAppId(), deviceAppForm.getDeviceId());
    }

    @DeleteMapping("/remove/device/")
    public AppDetail removeDeviceApp(@RequestBody DeviceAppForm deviceAppForm) {
        return appService.removeDeviceApp(deviceAppForm.getAppId(), deviceAppForm.getDeviceId());
    }


    
}
