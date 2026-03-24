package uis.iot.admin.services;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DeviceModelServiceI implements DeviceModelService {

    private static final Logger log = LoggerFactory.getLogger(DeviceModelServiceI.class);

    @Autowired private DeviceModelRepository modelRepository;
    @Autowired private ModelPropertyRepository modelPropertyRepository;

    @Override
    public List<DeviceModelDetail> getDeviceModelListByUserId(Long userId) {
        log.debug("Listando modelos para userId: {}", userId);
        Iterable<DeviceModel> deviceModels = modelRepository.findByUserId(userId);
        List<DeviceModelDetail> deviceModelList = new ArrayList<>();
        deviceModels.forEach(deviceModel -> {
            DeviceModelDetail d = new DeviceModelDetail();
            d.setEntity(deviceModel);
            deviceModelList.add(d);
        });
        log.debug("Se encontraron {} modelos para userId: {}", deviceModelList.size(), userId);
        return deviceModelList;
    }

    @Override
    public DeviceModelDetail getDeviceModelDetail(Long modelId) {
        log.debug("Buscando modelo con id: {}", modelId);
        DeviceModel deviceModel = modelRepository.findById(modelId).orElse(null);
        if (deviceModel != null) {
            DeviceModelDetail d = new DeviceModelDetail();
            d.setEntity(deviceModel);
            return d;
        }
        log.warn("Modelo no encontrado con id: {}", modelId);
        return null;
    }

    @Override
    public DeviceModelDetail createDeviceModel(DeviceModelForm deviceModelForm) {
        log.info("Creando nuevo modelo de dispositivo");
        DeviceModel deviceModel = deviceModelForm.getEntity();
        deviceModel = modelRepository.save(deviceModel);
        DeviceModelDetail d = new DeviceModelDetail();
        d.setEntity(deviceModel);
        log.info("Modelo creado con id: {}", deviceModel.getModelId());
        return d;
    }

    @Override
    public DeviceModelDetail updateDeviceModel(Long modelId, DeviceModelForm deviceModelForm) {
        log.info("Actualizando modelo con id: {}", modelId);
        DeviceModel deviceModel = modelRepository.findById(modelId).orElse(null);
        if (deviceModel != null) {
            deviceModelForm.setEntity(deviceModel);
            deviceModel = modelRepository.save(deviceModel);
            DeviceModelDetail d = new DeviceModelDetail();
            d.setEntity(deviceModel);
            log.info("Modelo actualizado con id: {}", modelId);
            return d;
        }
        log.warn("Modelo no encontrado para actualizar, id: {}", modelId);
        return null;
    }

    @Override
    public void deleteDeviceModel(Long modelId) {
        log.info("Eliminando modelo con id: {}", modelId);
        modelRepository.deleteById(modelId);
        log.info("Modelo eliminado con id: {}", modelId);
    }

    @Override
    public DeviceModelDetail addDeviceModelProperty(Long modelId, ModelPropertyForm propertyForm) {
        log.info("Añadiendo propiedad al modelo con id: {}", modelId);
        DeviceModel deviceModel = modelRepository.findById(modelId).orElse(null);
        if (deviceModel != null) {
            ModelProperty modelProperty = propertyForm.getEntity();
            modelProperty.setDeviceModel(deviceModel);
            modelPropertyRepository.save(modelProperty);
            DeviceModelDetail d = new DeviceModelDetail();
            d.setEntity(deviceModel);
            log.info("Propiedad añadida al modelo con id: {}", modelId);
            return d;
        }
        log.warn("Modelo no encontrado para añadir propiedad, id: {}", modelId);
        return null;
    }

    @Override
    public void deleteDeviceModelProperty(Long propertyId) {
        log.info("Eliminando propiedad de modelo con id: {}", propertyId);
        modelPropertyRepository.deleteById(propertyId);
        log.info("Propiedad eliminada con id: {}", propertyId);
    }
}