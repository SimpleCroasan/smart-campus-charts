package uis.iot.admin.services;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DeviceServiceI implements DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceServiceI.class);

    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceModelRepository modelRepository;
    @Autowired private DevicePropertyRepository devicePropertyRepository;

    @Override
    public List<DeviceDetail> getDeviceListByUserId(Long userId) {
        log.debug("Listando dispositivos para userId: {}", userId);
        Iterable<Device> devices = deviceRepository.findByUserId(userId);
        List<DeviceDetail> deviceList = new ArrayList<>();
        devices.forEach(device -> {
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            deviceList.add(deviceDetail);
        });
        log.debug("Se encontraron {} dispositivos para userId: {}", deviceList.size(), userId);
        return deviceList;
    }

    @Override
    public DeviceDetail getDeviceDetail(Long deviceId) {
        log.debug("Buscando dispositivo con id: {}", deviceId);
        Device device = deviceRepository.findById(deviceId).get();
        if (device != null) {
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            return deviceDetail;
        }
        log.warn("Dispositivo no encontrado con id: {}", deviceId);
        return null;
    }

    @Override
    public DeviceDetail createDevice(DeviceForm deviceForm) {
        log.info("Creando nuevo dispositivo");
        Device device = deviceForm.getEntity();
        if (device.getDeviceModel().getModelId() != null) {
            DeviceModel deviceModel = modelRepository.findById(device.getDeviceModel().getModelId()).orElse(null);
            if (deviceModel != null) {
                device.setDeviceModel(deviceModel);
                device = deviceRepository.save(device);
                DeviceDetail deviceDetail = new DeviceDetail();
                deviceDetail.setEntity(device);
                log.info("Dispositivo creado con id: {}", device.getDeviceId());
                return deviceDetail;
            }
            log.warn("Modelo no encontrado con id: {}", device.getDeviceModel().getModelId());
        } else {
            log.warn("Se intentó crear un dispositivo sin modelId");
        }
        return null;
    }

    @Override
    public DeviceDetail updateDevice(Long deviceId, DeviceForm deviceForm) {
        log.info("Actualizando dispositivo con id: {}", deviceId);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device != null && deviceForm.getModelId() != null) {
            DeviceModel deviceModel = modelRepository.findById(deviceForm.getModelId()).orElse(null);
            deviceForm.setEntity(device);
            device.setDeviceModel(deviceModel);
            device = deviceRepository.save(device);
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            log.info("Dispositivo actualizado con id: {}", deviceId);
            return deviceDetail;
        }
        log.warn("No se pudo actualizar dispositivo id: {}", deviceId);
        return null;
    }

    @Override
    public void deleteDevice(Long deviceId) {
        log.info("Eliminando dispositivo con id: {}", deviceId);
        deviceRepository.deleteById(deviceId);
        log.info("Dispositivo eliminado con id: {}", deviceId);
    }

    @Override
    public DeviceDetail addDeviceProperty(Long deviceId, DevicePropertyForm propertyForm) {
        log.info("Añadiendo propiedad al dispositivo con id: {}", deviceId);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device != null) {
            DeviceProperty deviceProperty = propertyForm.getEntity();
            deviceProperty.setDevice(device);
            devicePropertyRepository.save(deviceProperty);
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setEntity(device);
            log.info("Propiedad añadida al dispositivo con id: {}", deviceId);
            return deviceDetail;
        }
        log.warn("Dispositivo no encontrado, id: {}", deviceId);
        return null;
    }

    @Override
    public void deleteDeviceProperty(Long propertyId) {
        log.info("Eliminando propiedad de dispositivo con id: {}", propertyId);
        devicePropertyRepository.deleteById(propertyId);
        log.info("Propiedad eliminada con id: {}", propertyId);
    }
}