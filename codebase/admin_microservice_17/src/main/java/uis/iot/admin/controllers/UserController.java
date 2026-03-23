package uis.iot.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uis.iot.admin.dto.requests.UserForm;
import uis.iot.admin.dto.responses.UserDetail;
import uis.iot.admin.services.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/validate")
    public UserDetail validateUser(@RequestBody UserForm userData){
        return userService.validateUserData(userData);
    }    

    @GetMapping("/username/{userName}")
    public UserDetail getUserByUserName(@PathVariable String userName){
        return userService.getUserByUserName(userName);
    }

    @GetMapping("/usercode/{userUniqueCode}")
    public UserDetail getUserByUniqueCode(@PathVariable String userUniqueCode){
        return userService.getUserByUniqueCode(userUniqueCode);
    }

    @PostMapping("/new")
    public UserDetail createUser(@RequestBody UserForm newUserData){
        return userService.createUser(newUserData);
    }

    @PutMapping("/update/{userUniqueCode}")
    public UserDetail updateUserByUniqueCode(@RequestBody UserForm userData, @PathVariable String userUniqueCode){
        return userService.updateUserByUniqueCode(userData, userUniqueCode);
    }

    @DeleteMapping("/delete/{userUniqueCode}")
    public void deleteUserByUserUniqueCode(@PathVariable String userUniqueCode){
        userService.deleteUserByUserUniqueCode(userUniqueCode);
    }

}
