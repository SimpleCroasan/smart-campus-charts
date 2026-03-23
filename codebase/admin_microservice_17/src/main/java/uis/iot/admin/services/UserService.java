package uis.iot.admin.services;


import uis.iot.admin.dto.requests.UserForm;
import uis.iot.admin.dto.responses.UserDetail;

public interface UserService {

    /**
     * Valida los datos de un usuario
     * @param userName
     * @param userPin
     * @return {@link UserDetail}
     */
    UserDetail validateUserData(UserForm userData);

    /**
     * Retorna información de un usuario 
     * dado su nombre de Usuario
     * @param userName
     * @return {@link UserDetail}
     */
    UserDetail getUserByUserName(String userName);

    /**
     * Retorna información de un usuario 
     * dado su código Único
     * @param userUniqueCode
     * @return {@link UserDetail}
     */
    UserDetail getUserByUniqueCode(String userUniqueCode);

    /**
     * Guarda un nuevo usuario en base de datos
     * Retorna el usuario con el id asignado
     * @param newUserData
     * @return {@link UserDetail}
     */
    UserDetail createUser(UserForm newUserData);

    /**
     * Actualiza los datos de un usuario
     * @param userData
     * @return {@link UserDetail}
     */
    UserDetail updateUserByUniqueCode(UserForm userData,String userUniqueCode);

    /**
     * Elimina los datos de un usuario dado su Código Único
     * @param userUniqueCode
     */
    void deleteUserByUserUniqueCode(String userUniqueCode);
    

}
