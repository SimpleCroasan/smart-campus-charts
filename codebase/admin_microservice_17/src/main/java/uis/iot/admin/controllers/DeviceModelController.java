package uis.iot.admin.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uis.iot.admin.dto.requests.DeviceModelForm;
import uis.iot.admin.dto.requests.ModelPropertyForm;
import uis.iot.admin.dto.responses.DeviceModelDetail;
import uis.iot.admin.services.DeviceModelService;

@RestController
@RequestMapping("/api/v1/device-models")
public class DeviceModelController {

    private static final Logger log = LoggerFactory.getLogger(DeviceModelController.class);

    @Autowired private DeviceModelService deviceModelService;

    @GetMapping("/user/{userId}")
    public List<DeviceModelDetail> getDeviceModelListByUserId(@PathVariable Long userId) {
        log.info("GET /api/v1/device-models/user/{}", userId);
        return deviceModelService.getDeviceModelListByUserId(userId);
    }

    @GetMapping("/{modelId}")
    public DeviceModelDetail getDeviceModelDetail(@PathVariable Long modelId) {
        log.info("GET /api/v1/device-models/{}", modelId);
        return deviceModelService.getDeviceModelDetail(modelId);
    }

    @PostMapping("new")
    public DeviceModelDetail createDeviceModel(@RequestBody DeviceModelForm newDeviceData) {
        log.info("POST /api/v1/device-models/new");
        return deviceModelService.createDeviceModel(newDeviceData);
    }

    @PutMapping("update/{modelId}")
    public DeviceModelDetail updateDeviceModel(@PathVariable Long modelId, @RequestBody DeviceModelForm deviceData) {
        log.info("PUT /api/v1/device-models/update/{}", modelId);
        return deviceModelService.updateDeviceModel(modelId, deviceData);
    }

    @DeleteMapping("delete/{modelId}")
    public void deleteDeviceModel(@PathVariable Long modelId) {
        log.info("DELETE /api/v1/device-models/delete/{}", modelId);
        deviceModelService.deleteDeviceModel(modelId);
    }

    @PostMapping("add-property/{modelId}")
    public DeviceModelDetail addDeviceModelProperty(@PathVariable Long modelId, @RequestBody ModelPropertyForm data) {
        log.info("POST /api/v1/device-models/add-property/{}", modelId);
        return deviceModelService.addDeviceModelProperty(modelId, data);
    }

    @DeleteMapping("delete-property/{propertyId}")
    public void deleteDeviceModelProperty(@PathVariable Long propertyId) {
        log.info("DELETE /api/v1/device-models/delete-property/{}", propertyId);
        deviceModelService.deleteDeviceModelProperty(propertyId);
    }
}