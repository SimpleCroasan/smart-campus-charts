package uis.iot.admin.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uis.iot.admin.dto.requests.UserForm;
import uis.iot.admin.dto.responses.UserDetail;
import uis.iot.admin.models.User;
import uis.iot.admin.repositories.UserRepository;

@Service
public class UserServiceI  implements UserService{

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetail validateUserData(UserForm userData) {
        User user = userRepository.findByUserName(userData.getUserName());        
        UserDetail userDetail = new UserDetail();
        if (user != null && user.getPassword().equals(userData.getPassword())){
            userDetail.setEntity(user);            
        }
        return userDetail;
    }


    /**
     * Retrieves the user details by the given username.
     *
     * @param  userName  the username of the user
     * @return          the user details associated with the username
     */
    public UserDetail getUserByUserName(String userName){
        User user = userRepository.findByUserName(userName);       
        if (user != null){        
            UserDetail userDetail = new UserDetail();    
            userDetail.setEntity(user);
            return userDetail;            
        } 
        return null;        
    }

    /**
     * Retrieves user details by unique code.
     *
     * @param  userUniqueCode    the unique code of the user
     * @return                  the user details
     */
    public UserDetail getUserByUniqueCode(String userUniqueCode){
        User user = userRepository.findByUserUniqueCode(userUniqueCode);
        if (user != null){
            UserDetail userDetail = new UserDetail();
            userDetail.setEntity(user);
            return userDetail;
        }
        return null;
    }

    /**
     * Creates a new user based on the provided user form data.
     *
     * @param  newUserData  the user form data to create the user from
     * @return              the user detail of the newly created user
     */
    public UserDetail createUser(UserForm newUserData){
        User user = newUserData.getEntity();
        UUID uuid = UUID.randomUUID();
        user.setUserUniqueCode(uuid.toString());
        userRepository.save(user);
        UserDetail userDetail = new UserDetail();
        userDetail.setEntity(user);
        return userDetail;
    }


    /**
     * Updates the user details based on the provided user form data.
     *
     * @param  userData  the user form data to update the user details
     * @return           the user detail of the updated user
     */
    public UserDetail updateUserByUniqueCode(UserForm userData,String userUniqueCode){
        User user = userRepository.findByUserUniqueCode(userUniqueCode);
        if (user != null){
            user.setUserName(userData.getUserName());
            UserDetail userDetail = new UserDetail();
            userDetail.setEntity(userRepository.save(user));
            return userDetail;
        }
        return null;        
    }

    
    /**
     * Deletes a user by user unique code.
     *
     * @param  userUniqueCode    the unique code of the user to be deleted
     * @return                   void
     */
    public void deleteUserByUserUniqueCode(String userUniqueCode){
        System.out.println("Deleting user with unique code: " + userUniqueCode);
        User user = userRepository.findByUserUniqueCode(userUniqueCode);
        if (user != null){
            userRepository.delete(user);
        }        
    }
    
}
