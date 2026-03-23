package uis.iot.admin.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uis.iot.admin.dto.requests.DeviceModelForm;
import uis.iot.admin.dto.requests.ModelPropertyForm;
import uis.iot.admin.dto.responses.DeviceModelDetail;
import uis.iot.admin.models.DeviceModel;
import uis.iot.admin.models.ModelProperty;
import uis.iot.admin.repositories.DeviceModelRepository;
import uis.iot.admin.repositories.ModelPropertyRepository;

@Service
public class DeviceModelServiceI implements DeviceModelService{

    @Autowired
    private DeviceModelRepository modelRepository;

    @Autowired
    private ModelPropertyRepository modelPropertyRepository;

    @Override
    public List<DeviceModelDetail> getDeviceModelListByUserId(Long userId) {
        Iterable<DeviceModel> deviceModels = modelRepository.findByUserId(userId);
        List<DeviceModelDetail> deviceModelList = new ArrayList<>();

        deviceModels.forEach(deviceModel -> {
            DeviceModelDetail deviceModelDetail = new DeviceModelDetail();
            deviceModelDetail.setEntity(deviceModel);
            deviceModelList.add(deviceModelDetail);
        });

        return deviceModelList;          
    }

    @Override
    public DeviceModelDetail getDeviceModelDetail(Long modelId) {
        DeviceModel deviceModel = modelRepository.findById(modelId).orElse(null);
        if(deviceModel != null){
            DeviceModelDetail deviceModelDetail = new DeviceModelDetail();
            deviceModelDetail.setEntity(deviceModel);
            return deviceModelDetail;
        }

        return null;        
    }

    @Override
    public DeviceModelDetail createDeviceModel(DeviceModelForm deviceModelForm) {
        DeviceModel deviceModel =deviceModelForm.getEntity();
        deviceModel = modelRepository.save(deviceModel);
        DeviceModelDetail deviceModelDetail = new DeviceModelDetail();
        deviceModelDetail.setEntity(deviceModel);
        return deviceModelDetail;
    }

    @Override
    public DeviceModelDetail updateDeviceModel(Long modelId, DeviceModelForm deviceModelForm) {
        DeviceModel deviceModel = modelRepository.findById(modelId).orElse(null);
        if(deviceModel != null){
            deviceModelForm.setEntity(deviceModel);
            deviceModel = modelRepository.save(deviceModel);
            DeviceModelDetail deviceModelDetail = new DeviceModelDetail();
            deviceModelDetail.setEntity(deviceModel);
            return deviceModelDetail;
        }
        return null;
    }

    @Override
    public void deleteDeviceModel(Long modelId) {
        modelRepository.deleteById(modelId);
    }

    @Override
    public DeviceModelDetail addDeviceModelProperty(Long modelId, ModelPropertyForm propertyForm) {
        DeviceModel deviceModel = modelRepository.findById(modelId).orElse(null);
        if(deviceModel != null){
            ModelProperty modelProperty = propertyForm.getEntity();
            modelProperty.setDeviceModel(deviceModel);
            modelProperty = modelPropertyRepository.save(modelProperty);
            DeviceModelDetail deviceModelDetail = new DeviceModelDetail();
            deviceModelDetail.setEntity(deviceModel);
            return deviceModelDetail;
        
        }
        return null;        
    }

    @Override
    public void deleteDeviceModelProperty(Long propertyId) {
        System.out.println("entro");
        modelPropertyRepository.deleteById(propertyId);
    }

}
