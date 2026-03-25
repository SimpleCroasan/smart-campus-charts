package uis.iot.admin.services;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uis.iot.admin.dto.requests.AppForm;
import uis.iot.admin.dto.responses.AppDetail;
import uis.iot.admin.models.App;
import uis.iot.admin.models.Device;
import uis.iot.admin.repositories.AppRepository;
import uis.iot.admin.repositories.DeviceRepository;

@Service
public class AppServiceI implements AppService {

    private static final Logger log = LoggerFactory.getLogger(AppServiceI.class);

    @Autowired private AppRepository appRepository;
    @Autowired private DeviceRepository deviceRepository;

    @Override
    public List<AppDetail> getAppListByUserId(Long userId) {
        log.debug("Listando aplicaciones para userId: {}", userId);
        Iterable<App> apps = appRepository.findByUserId(userId);
        List<AppDetail> appList = new ArrayList<>();
        apps.forEach(app -> {
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(app);
            appList.add(appDetail);
        });
        log.debug("Se encontraron {} aplicaciones para userId: {}", appList.size(), userId);
        return appList;
    }

    @Override
    public AppDetail getAppDetail(Long appId) {
        log.debug("Buscando aplicación con id: {}", appId);
        App app = appRepository.findById(appId).orElse(null);
        if (app != null) {
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(app);
            return appDetail;
        }
        log.warn("Aplicación no encontrada con id: {}", appId);
        return null;
    }

    @Override
    public AppDetail createApp(AppForm appForm) {
        log.info("Creando nueva aplicación");
        App app = appForm.getEntity();
        app = appRepository.save(app);
        AppDetail appDetail = new AppDetail();
        appDetail.setEntity(app);
        log.info("Aplicación creada con id: {}", app.getAppId());
        return appDetail;
    }

    @Override
    public AppDetail updateApp(Long appId, AppForm appForm) {
        log.info("Actualizando aplicación con id: {}", appId);
        App app = appRepository.findById(appId).orElse(null);
        if (app != null) {
            appForm.setEntity(app);
            app = appRepository.save(app);
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(appRepository.save(app));
            log.info("Aplicación actualizada con id: {}", appId);
            return appDetail;
        }
        log.warn("Aplicación no encontrada para actualizar, id: {}", appId);
        return null;
    }

    @Override
    public void deleteApp(Long appId) {
        log.info("Eliminando aplicación con id: {}", appId);
        appRepository.deleteById(appId);
        log.info("Aplicación eliminada con id: {}", appId);
    }

    @Override
    public AppDetail addDeviceApp(Long appId, Long deviceId) {
        log.info("Añadiendo dispositivo {} a la aplicación {}", deviceId, appId);
        App app = appRepository.findById(appId).orElse(null);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (app != null && device != null) {
            if (!app.getAppDevices().contains(device)) {
                app.getAppDevices().add(device);
                app = appRepository.save(app);
                AppDetail appDetail = new AppDetail();
                appDetail.setEntity(app);
                log.info("Dispositivo {} añadido a la aplicación {}", deviceId, appId);
                return appDetail;
            }
            log.warn("El dispositivo {} ya está asociado a la aplicación {}", deviceId, appId);
        } else {
            log.warn("No se encontró aplicación {} o dispositivo {}", appId, deviceId);
        }
        return null;
    }

    @Override
    public AppDetail removeDeviceApp(Long appId, Long deviceId) {
        log.info("Removiendo dispositivo {} de la aplicación {}", deviceId, appId);
        App app = appRepository.findById(appId).orElse(null);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (app != null && device != null) {
            app.getAppDevices().remove(device);
            app = appRepository.save(app);
            AppDetail appDetail = new AppDetail();
            appDetail.setEntity(app);
            log.info("Dispositivo {} removido de la aplicación {}", deviceId, appId);
            return appDetail;
        }
        log.warn("No se encontró aplicación {} o dispositivo {}", appId, deviceId);
        return null;
    }
}