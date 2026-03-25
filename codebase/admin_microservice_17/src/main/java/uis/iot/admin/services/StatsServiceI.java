package uis.iot.admin.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uis.iot.admin.dto.responses.StatsDetail;
import uis.iot.admin.repositories.AppRepository;
import uis.iot.admin.repositories.DeviceModelRepository;
import uis.iot.admin.repositories.DeviceRepository;

@Service
public class StatsServiceI implements StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsServiceI.class);

    @Autowired private AppRepository appRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceModelRepository modelRepository;

    @Override
    public StatsDetail getStats(Long userId) {
        log.debug("Calculando estadísticas para userId: {}", userId);
        Long totalDevices = deviceRepository.countByUserId(userId);
        Long totalModels  = modelRepository.countByUserId(userId);
        Long totalApps    = appRepository.countByUserId(userId);
        StatsDetail statsDetail = new StatsDetail();
        statsDetail.setTotalDevices(totalDevices);
        statsDetail.setTotalModels(totalModels);
        statsDetail.setTotalApps(totalApps);
        log.debug("Stats userId {}: devices={}, models={}, apps={}", userId, totalDevices, totalModels, totalApps);
        return statsDetail;
    }
}