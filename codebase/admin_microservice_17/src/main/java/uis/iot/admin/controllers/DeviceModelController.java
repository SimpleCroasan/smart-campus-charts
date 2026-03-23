package uis.iot.admin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import uis.iot.admin.dto.requests.DeviceModelForm;
import uis.iot.admin.dto.requests.ModelPropertyForm;
import uis.iot.admin.dto.responses.DeviceModelDetail;
import uis.iot.admin.services.DeviceModelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/v1/device-models")
public class DeviceModelController {
    
    @Autowired
    private DeviceModelService deviceModelService;

    @GetMapping("/user/{userId}")
    public List<DeviceModelDetail> getDeviceModelListByUserId(@PathVariable Long userId){
        return deviceModelService.getDeviceModelListByUserId(userId);
    }

    @GetMapping("/{modelId}")
    public DeviceModelDetail getDeviceModelDetail(@PathVariable Long modelId){
        return deviceModelService.getDeviceModelDetail(modelId);
    }

    @PostMapping("new")
    public DeviceModelDetail createDeviceModel(@RequestBody DeviceModelForm newDeviceData) {
        return deviceModelService.createDeviceModel(newDeviceData);
    }

    @PutMapping("update/{modelId}")
    public DeviceModelDetail updateDeviceModel(@PathVariable Long modelId, @RequestBody DeviceModelForm deviceData) {
        return deviceModelService.updateDeviceModel(modelId, deviceData);
    }

    @DeleteMapping("delete/{modelId}")
    public void deleteDeviceModel(@PathVariable Long modelId) {
        deviceModelService.deleteDeviceModel(modelId);
    }

    @PostMapping("add-property/{modelId}")
    public DeviceModelDetail addDeviceModelProperty(@PathVariable Long modelId, @RequestBody ModelPropertyForm modelPropertyData) {
        return deviceModelService.addDeviceModelProperty(modelId, modelPropertyData);
    }

    @DeleteMapping("delete-property/{propertyId}")
    public void deleteDeviceModelProperty(@PathVariable Long propertyId) {
        deviceModelService.deleteDeviceModelProperty(propertyId);
    }

}
