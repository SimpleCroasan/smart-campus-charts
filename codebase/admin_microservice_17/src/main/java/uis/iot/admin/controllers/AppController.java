package uis.iot.admin.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uis.iot.admin.dto.requests.AppForm;
import uis.iot.admin.dto.requests.DeviceAppForm;
import uis.iot.admin.dto.responses.AppDetail;
import uis.iot.admin.services.AppService;

@RestController
@RequestMapping("/api/v1/apps")
public class AppController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    @Autowired private AppService appService;

    @GetMapping("/user/{userId}")
    public List<AppDetail> getAppListByUserId(@PathVariable Long userId) {
        log.info("GET /api/v1/apps/user/{}", userId);
        return appService.getAppListByUserId(userId);
    }

    @GetMapping("/{appId}")
    public AppDetail getAppDetail(@PathVariable Long appId) {
        log.info("GET /api/v1/apps/{}", appId);
        return appService.getAppDetail(appId);
    }

    @PostMapping("/new")
    public AppDetail createApp(@RequestBody AppForm newAppData) {
        log.info("POST /api/v1/apps/new");
        return appService.createApp(newAppData);
    }

    @PutMapping("/update/{appId}")
    public AppDetail updateApp(@PathVariable Long appId, @RequestBody AppForm newAppData) {
        log.info("PUT /api/v1/apps/update/{}", appId);
        return appService.updateApp(appId, newAppData);
    }

    @DeleteMapping("/delete/{appId}")
    public void deleteApp(@PathVariable Long appId) {
        log.info("DELETE /api/v1/apps/delete/{}", appId);
        appService.deleteApp(appId);
    }

    @PostMapping("/add/device/")
    public AppDetail addDeviceApp(@RequestBody DeviceAppForm deviceAppForm) {
        log.info("POST /api/v1/apps/add/device/ - appId: {}, deviceId: {}",
                deviceAppForm.getAppId(), deviceAppForm.getDeviceId());
        return appService.addDeviceApp(deviceAppForm.getAppId(), deviceAppForm.getDeviceId());
    }

    @DeleteMapping("/remove/device/")
    public AppDetail removeDeviceApp(@RequestBody DeviceAppForm deviceAppForm) {
        log.info("DELETE /api/v1/apps/remove/device/ - appId: {}, deviceId: {}",
                deviceAppForm.getAppId(), deviceAppForm.getDeviceId());
        return appService.removeDeviceApp(deviceAppForm.getAppId(), deviceAppForm.getDeviceId());
    }
}