package uis.iot.admin.services;

import java.util.List;

import uis.iot.admin.dto.requests.DeviceForm;
import uis.iot.admin.dto.requests.DevicePropertyForm;
import uis.iot.admin.dto.responses.DeviceDetail;

public interface DeviceService {

    /**
     * Retorna listado de dispositivos de un usuario
     * @param userId
     * @return {@link DeviceDetail}
     */
    List<DeviceDetail> getDeviceListByUserId(Long userId);
    
    /**
    * Retorna la información de un dispositivo por su id
    * @param deviceId
    * @return {@link DeviceDetail}
    */
    DeviceDetail getDeviceDetail(Long deviceId);

    
    /**
     * Permite crear un nuevo dispositivo
     * retornando la información del mismo
     * @param deviceForm
     * @return {@link DeviceDetail}
     */
    DeviceDetail createDevice(DeviceForm deviceForm);

    /**
     * Permite actualizar los datos de un dispositivo
     * @param deviceId
     * @param deviceForm
     * @return {@link DeviceDetail}
     */
    DeviceDetail updateDevice(Long deviceId,DeviceForm deviceForm);

    /**
     * Permite borrar un dispositivo
     * @param deviceId
     */
    void deleteDevice(Long deviceId);

    /**
     * Permite agregar una propiedad al dispositivo
     * @param deviceId
     * @param propertyForm
     * @return {@link DeviceDetail}
     */
    DeviceDetail addDeviceProperty(Long modelId,DevicePropertyForm propertyForm);

    /**
     * Permite eliminar una propiedad al modelo de dispositivo
     * @param devicePropertyId
     */
    void deleteDeviceProperty(Long devicePropertyId);
}
