package uis.iot.admin.services;


import java.util.List;

import uis.iot.admin.dto.requests.AppForm;
import uis.iot.admin.dto.responses.AppDetail;

public interface AppService {

    /**
     * Retorna listado de aplicaciones de un usuario
     * @param userId
     * @return {@link AppDetail}
     */
    List<AppDetail> getAppListByUserId(Long userId);

    /**
     * Retorna información de la aplicación
     * @param appId
     * @return {@link AppDetail}
     */
    AppDetail getAppDetail(Long appId);

    /**
     * Permite crear una nueva aplicación
     * retornando la información de la misma
     * @param appForm
     * @return {@link AppDetail}
     */
    AppDetail createApp(AppForm appForm);

    /**
     * Permite actualizar los datos de una aplicación
     * @param appId
     * @return {@link AppDetail}
     */
    AppDetail updateApp(Long appId,AppForm appForm);

    /**
     * Permite borrar una aplicación
     * @param appId
     */
    void deleteApp(Long appId); 
    
    /**
     * Permite asociar un dispositivo a la aplicacion
     */
    AppDetail addDeviceApp(Long appId,Long deviceId);

    /**
     * Permite desasociar un dispositivo a la aplicacion
     */
    AppDetail removeDeviceApp(Long appId,Long deviceId);
    
}
