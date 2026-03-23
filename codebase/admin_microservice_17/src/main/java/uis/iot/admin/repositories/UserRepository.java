package uis.iot.admin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uis.iot.admin.models.User;

public interface UserRepository extends JpaRepository<User,Long>{

    User findByUserName(String userName);

    User findByUserUniqueCode(String userUniqueCode);    
}
