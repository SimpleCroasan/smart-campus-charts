package uis.iot.admin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uis.iot.admin.dto.requests.DeviceForm;
import uis.iot.admin.dto.requests.DevicePropertyForm;
import uis.iot.admin.dto.responses.DeviceDetail;
import uis.iot.admin.services.DeviceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;
    
    /**
     * Retorna listado de dispositivos de un usuario
     * @param userId
     * @return {@link DeviceDetail}
     */
    @GetMapping("/user/{userId}")
    public List<DeviceDetail> getDeviceListByUserId(@PathVariable Long userId) {
        return deviceService.getDeviceListByUserId(userId);
    }

    /**
     * Retorna la información de un dispositivo por su id
     * @param deviceId
     * @return {@link DeviceDetail}
     */
    @GetMapping("/{deviceId}")
    public DeviceDetail getDeviceDetail(@PathVariable Long deviceId) {
        return deviceService.getDeviceDetail(deviceId);
    }
    
    @PostMapping("new")
    public DeviceDetail createDevice(@RequestBody DeviceForm newDeviceData) {
        return deviceService.createDevice(newDeviceData);
    }

    @PutMapping("update/{deviceId}")
    public DeviceDetail updateDevice(@PathVariable Long deviceId, @RequestBody DeviceForm deviceData) {
        return deviceService.updateDevice(deviceId, deviceData);
    }

    @DeleteMapping("delete/{deviceId}")
    public void deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
    }

    @PostMapping("add-property/{deviceId}")
    public DeviceDetail addDeviceProperty(@PathVariable Long deviceId, @RequestBody DevicePropertyForm devicePropertyData) {
        return deviceService.addDeviceProperty(deviceId, devicePropertyData);
    }

    @DeleteMapping("delete-property/{propertyId}")
    public void deleteDeviceProperty(@PathVariable Long propertyId) {
        deviceService.deleteDeviceProperty(propertyId);
    }
    

}
