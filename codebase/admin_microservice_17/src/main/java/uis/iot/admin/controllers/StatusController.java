package uis.iot.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uis.iot.admin.dto.responses.StatsDetail;
import uis.iot.admin.dto.responses.StatusDetail;
import uis.iot.admin.services.StatsService;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public StatusDetail getStatus(){
        StatusDetail status = new StatusDetail();
        status.setStatus("200");
        status.setMessage("ADMIN SERVICE IS UP AND RUNNING");
        return status;
    }

    @GetMapping("/stats/{userId}")
    public StatsDetail getStats(@PathVariable Long userId){
        return statsService.getStats(userId);
    }

    

    
}
