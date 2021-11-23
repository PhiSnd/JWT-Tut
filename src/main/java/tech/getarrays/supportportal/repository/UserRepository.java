package tech.getarrays.supportportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.getarrays.supportportal.domain.User;


//for accessing the database
//JpaRepository has methods we can use to create querys
public interface UserRepository extends JpaRepository<User,Long> {

    User findUserByUsername(String username);
    User findUserByEmail(String email);

}
