package uis.iot.admin.services;

import uis.iot.admin.dto.responses.StatsDetail;

public interface StatsService {

    /*
     * Get stats of all devices, models and apps
     */
    
     public StatsDetail getStats(Long userId);

}
