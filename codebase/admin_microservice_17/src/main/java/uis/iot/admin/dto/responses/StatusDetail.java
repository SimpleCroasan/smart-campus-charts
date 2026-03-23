package uis.iot.admin.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusDetail {

    private String status;
    private String message;

    public void setStatus(String status, String message){
        this.status = status;
        this.message = message;
    }
    
}
