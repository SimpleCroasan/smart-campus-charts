package uis.iot.admin.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uis.iot.admin.dto.requests.UserForm;
import uis.iot.admin.dto.responses.UserDetail;
import uis.iot.admin.services.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserService userService;

    @PostMapping("/validate")
    public UserDetail validateUser(@RequestBody UserForm userData) {
        log.info("POST /api/v1/user/validate - username: {}", userData.getUserName());
        return userService.validateUserData(userData);
    }

    @GetMapping("/username/{userName}")
    public UserDetail getUserByUserName(@PathVariable String userName) {
        log.info("GET /api/v1/user/username/{}", userName);
        return userService.getUserByUserName(userName);
    }

    @GetMapping("/usercode/{userUniqueCode}")
    public UserDetail getUserByUniqueCode(@PathVariable String userUniqueCode) {
        log.info("GET /api/v1/user/usercode/{}", userUniqueCode);
        return userService.getUserByUniqueCode(userUniqueCode);
    }

    @PostMapping("/new")
    public UserDetail createUser(@RequestBody UserForm newUserData) {
        log.info("POST /api/v1/user/new - username: {}", newUserData.getUserName());
        return userService.createUser(newUserData);
    }

    @PutMapping("/update/{userUniqueCode}")
    public UserDetail updateUserByUniqueCode(@RequestBody UserForm userData, @PathVariable String userUniqueCode) {
        log.info("PUT /api/v1/user/update/{}", userUniqueCode);
        return userService.updateUserByUniqueCode(userData, userUniqueCode);
    }

    @DeleteMapping("/delete/{userUniqueCode}")
    public void deleteUserByUserUniqueCode(@PathVariable String userUniqueCode) {
        log.info("DELETE /api/v1/user/delete/{}", userUniqueCode);
        userService.deleteUserByUserUniqueCode(userUniqueCode);
    }
}