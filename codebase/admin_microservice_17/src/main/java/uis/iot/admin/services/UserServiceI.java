package uis.iot.admin.services;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uis.iot.admin.dto.requests.UserForm;
import uis.iot.admin.dto.responses.UserDetail;
import uis.iot.admin.models.User;
import uis.iot.admin.repositories.UserRepository;

@Service
public class UserServiceI implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceI.class);

    @Autowired private UserRepository userRepository;

    @Override
    public UserDetail validateUserData(UserForm userData) {
        log.debug("Validando credenciales para usuario: {}", userData.getUserName());
        User user = userRepository.findByUserName(userData.getUserName());
        UserDetail userDetail = new UserDetail();
        if (user != null && user.getPassword().equals(userData.getPassword())) {
            userDetail.setEntity(user);
            log.info("Autenticación exitosa para usuario: {}", userData.getUserName());
        } else {
            log.warn("Autenticación fallida para usuario: {}", userData.getUserName());
        }
        return userDetail;
    }

    public UserDetail getUserByUserName(String userName) {
        log.debug("Buscando usuario por username: {}", userName);
        User user = userRepository.findByUserName(userName);
        if (user != null) {
            UserDetail userDetail = new UserDetail();
            userDetail.setEntity(user);
            return userDetail;
        }
        log.warn("Usuario no encontrado: {}", userName);
        return null;
    }

    public UserDetail getUserByUniqueCode(String userUniqueCode) {
        log.debug("Buscando usuario por código único: {}", userUniqueCode);
        User user = userRepository.findByUserUniqueCode(userUniqueCode);
        if (user != null) {
            UserDetail userDetail = new UserDetail();
            userDetail.setEntity(user);
            return userDetail;
        }
        log.warn("Usuario no encontrado con código único: {}", userUniqueCode);
        return null;
    }

    public UserDetail createUser(UserForm newUserData) {
        log.info("Creando nuevo usuario: {}", newUserData.getUserName());
        User user = newUserData.getEntity();
        UUID uuid = UUID.randomUUID();
        user.setUserUniqueCode(uuid.toString());
        userRepository.save(user);
        UserDetail userDetail = new UserDetail();
        userDetail.setEntity(user);
        log.info("Usuario creado con código único: {}", uuid);
        return userDetail;
    }

    public UserDetail updateUserByUniqueCode(UserForm userData, String userUniqueCode) {
        log.info("Actualizando usuario con código único: {}", userUniqueCode);
        User user = userRepository.findByUserUniqueCode(userUniqueCode);
        if (user != null) {
            user.setUserName(userData.getUserName());
            UserDetail userDetail = new UserDetail();
            userDetail.setEntity(userRepository.save(user));
            log.info("Usuario actualizado: {}", userUniqueCode);
            return userDetail;
        }
        log.warn("No se encontró usuario para actualizar: {}", userUniqueCode);
        return null;
    }

    public void deleteUserByUserUniqueCode(String userUniqueCode) {
        log.info("Eliminando usuario con código único: {}", userUniqueCode);
        User user = userRepository.findByUserUniqueCode(userUniqueCode);
        if (user != null) {
            userRepository.delete(user);
            log.info("Usuario eliminado: {}", userUniqueCode);
        } else {
            log.warn("No se encontró usuario para eliminar: {}", userUniqueCode);
        }
    }
}