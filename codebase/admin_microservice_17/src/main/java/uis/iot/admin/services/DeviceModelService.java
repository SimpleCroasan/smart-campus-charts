package uis.iot.admin.services;

import java.util.List;

import uis.iot.admin.dto.requests.DeviceModelForm;
import uis.iot.admin.dto.requests.ModelPropertyForm;
import uis.iot.admin.dto.responses.DeviceModelDetail;

public interface DeviceModelService {


    /**
     * Retorna listado de modelos de dispositivos de un usuario
     * @param userId
     * @return {@link DeviceModelDetail}
     */
    List<DeviceModelDetail> getDeviceModelListByUserId(Long userId);

    /**
    * Retorna la información de un modelo por su id
    * @param modelId
    * @return {@link DeviceModelDetail}
    */
    DeviceModelDetail getDeviceModelDetail(Long modelId);

    /**
     * Permite crear un nuevo modelo de dispositivo
     * retornando la información del mismo
     * @param deviceModelForm
     * @return {@link DeviceModelDetail}
     */
    DeviceModelDetail createDeviceModel(DeviceModelForm deviceModelForm);

    /**
     * Permite actualizar los datos de un modelo de dispositivo
     * @param modelId
     * @param deviceModelForm
     * @return {@link DeviceModelDetail}
     */
    DeviceModelDetail updateDeviceModel(Long modelId,DeviceModelForm deviceModelForm);

    /**
     * Permite borrar un modelo de dispositivo
     * @param modelId
     */
    void deleteDeviceModel(Long modelId);

    /**
     * Permite agregar una propiedad al modelo de dispositivo
     * @param modelId
     * @param propertyForm
     * @return {@link DeviceModelDetail}
     */
    DeviceModelDetail addDeviceModelProperty(Long modelId,ModelPropertyForm propertyForm);

    /**
     * Permite eliminar una propiedad al modelo de dispositivo
     * @param modelPropertyId
     */
    void deleteDeviceModelProperty(Long modelPropertyId);
    
}
