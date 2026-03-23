package uis.iot.admin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uis.iot.admin.dto.responses.StatsDetail;
import uis.iot.admin.repositories.AppRepository;
import uis.iot.admin.repositories.DeviceModelRepository;
import uis.iot.admin.repositories.DeviceRepository;

@Service
public class StatsServiceI implements StatsService {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceModelRepository modelRepository;

    @Override
    public StatsDetail getStats(Long userId) {
        Long totalDevices = deviceRepository.countByUserId(userId);
        Long totalModels = modelRepository.countByUserId(userId);
        Long totalApps = appRepository.countByUserId(userId);
        StatsDetail statsDetail = new StatsDetail();
        statsDetail.setTotalDevices(totalDevices);
        statsDetail.setTotalModels(totalModels);
        statsDetail.setTotalApps(totalApps);
        return statsDetail;
    }
    
}
