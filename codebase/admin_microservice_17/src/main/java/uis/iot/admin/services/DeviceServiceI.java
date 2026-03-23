package uis.iot.admin.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uis.iot.admin.dto.requests.DeviceForm;
import uis.iot.admin.dto.requests.DevicePropertyForm;
import uis.iot.admin.dto.responses.DeviceDetail;
import uis.iot.admin.models.Device;
import uis.iot.admin.models.DeviceModel;
import uis.iot.admin.models.DeviceProperty;
import uis.iot.admin.repositories.DeviceModelRepository;
import uis.iot.admin.repositories.DevicePropertyRepository;
import uis.iot.admin.repositories.DeviceRepository;

@Service
public class DeviceServiceI implements DeviceService{

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceModelRepository modelRepository;

    @Autowired
    private DevicePropertyRepository devicePropertyRepository;

    @Override
    public List<DeviceDetail> getDeviceListByUserId(Long userId) {
        Iterable<Device> devices = deviceRepository.findByUserId(userId);
        List<DeviceDetail> deviceList = new ArrayList<>();

        devices.forEach(device -> {
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            deviceList.add(deviceDetail);
        });

        return deviceList;   
    }

    @Override
    public DeviceDetail getDeviceDetail(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).get();
        if(device != null){
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            return deviceDetail;
        }
        return null;
    }

    @Override
    public DeviceDetail createDevice(DeviceForm deviceForm) {
        Device device = deviceForm.getEntity();
        if(device.getDeviceModel().getModelId() != null){
            DeviceModel deviceModel = modelRepository.findById(device.getDeviceModel().getModelId()).orElse(null);
            if(deviceModel != null){
                device.setDeviceModel(deviceModel);
                device = deviceRepository.save(device);
                DeviceDetail deviceDetail = new DeviceDetail();
                deviceDetail.setEntity(device);
                return deviceDetail;            
            }
        }       
        return null;       
    }

    @Override
    public DeviceDetail updateDevice(Long deviceId, DeviceForm deviceForm) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if(device != null && deviceForm.getModelId() != null){
            DeviceModel deviceModel = modelRepository.findById(deviceForm.getModelId()).orElse(null);
            deviceForm.setEntity(device);
            device.setDeviceModel(deviceModel);
            device = deviceRepository.save(device);
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            return deviceDetail;
        }
        return null;
    }

    @Override
    public void deleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }

    @Override
    public DeviceDetail addDeviceProperty(Long deviceId, DevicePropertyForm propertyForm) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if(device != null){
            DeviceProperty deviceProperty = propertyForm.getEntity();
            deviceProperty.setDevice(device);
            deviceProperty = devicePropertyRepository.save(deviceProperty);      
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            return deviceDetail;
        }
        return null;        
    }

    @Override
    public void deleteDeviceProperty(Long propertyId) {
        devicePropertyRepository.deleteById(propertyId);
    }



}
