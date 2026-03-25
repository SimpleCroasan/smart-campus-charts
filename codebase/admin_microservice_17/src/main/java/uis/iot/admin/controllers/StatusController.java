package uis.iot.admin.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uis.iot.admin.dto.responses.StatsDetail;
import uis.iot.admin.dto.responses.StatusDetail;
import uis.iot.admin.services.StatsService;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    private static final Logger log = LoggerFactory.getLogger(StatusController.class);

    @Autowired private StatsService statsService;

    @GetMapping
    public StatusDetail getStatus() {
        log.debug("GET /api/v1/status - health check");
        StatusDetail status = new StatusDetail();
        status.setStatus("200");
        status.setMessage("ADMIN SERVICE IS UP AND RUNNING");
        return status;
    }

    @GetMapping("/stats/{userId}")
    public StatsDetail getStats(@PathVariable Long userId) {
        log.info("GET /api/v1/status/stats/{}", userId);
        return statsService.getStats(userId);
    }
}