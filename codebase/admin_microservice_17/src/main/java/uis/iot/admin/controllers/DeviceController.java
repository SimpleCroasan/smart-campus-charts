package uis.iot.admin.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uis.iot.admin.dto.requests.DeviceForm;
import uis.iot.admin.dto.requests.DevicePropertyForm;
import uis.iot.admin.dto.responses.DeviceDetail;
import uis.iot.admin.services.DeviceService;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired private DeviceService deviceService;

    @GetMapping("/user/{userId}")
    public List<DeviceDetail> getDeviceListByUserId(@PathVariable Long userId) {
        log.info("GET /api/v1/devices/user/{}", userId);
        return deviceService.getDeviceListByUserId(userId);
    }

    @GetMapping("/{deviceId}")
    public DeviceDetail getDeviceDetail(@PathVariable Long deviceId) {
        log.info("GET /api/v1/devices/{}", deviceId);
        return deviceService.getDeviceDetail(deviceId);
    }

    @PostMapping("new")
    public DeviceDetail createDevice(@RequestBody DeviceForm newDeviceData) {
        log.info("POST /api/v1/devices/new");
        return deviceService.createDevice(newDeviceData);
    }

    @PutMapping("update/{deviceId}")
    public DeviceDetail updateDevice(@PathVariable Long deviceId, @RequestBody DeviceForm deviceData) {
        log.info("PUT /api/v1/devices/update/{}", deviceId);
        return deviceService.updateDevice(deviceId, deviceData);
    }

    @DeleteMapping("delete/{deviceId}")
    public void deleteDevice(@PathVariable Long deviceId) {
        log.info("DELETE /api/v1/devices/delete/{}", deviceId);
        deviceService.deleteDevice(deviceId);
    }

    @PostMapping("add-property/{deviceId}")
    public DeviceDetail addDeviceProperty(@PathVariable Long deviceId, @RequestBody DevicePropertyForm data) {
        log.info("POST /api/v1/devices/add-property/{}", deviceId);
        return deviceService.addDeviceProperty(deviceId, data);
    }

    @DeleteMapping("delete-property/{propertyId}")
    public void deleteDeviceProperty(@PathVariable Long propertyId) {
        log.info("DELETE /api/v1/devices/delete-property/{}", propertyId);
        deviceService.deleteDeviceProperty(propertyId);
    }
}