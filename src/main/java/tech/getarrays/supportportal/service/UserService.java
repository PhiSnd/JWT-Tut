package tech.getarrays.supportportal.service;

import tech.getarrays.supportportal.domain.User;
import tech.getarrays.supportportal.exception.domain.EmailExistException;
import tech.getarrays.supportportal.exception.domain.UsernameExistException;

import java.util.List;

public interface UserService {

    User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);
}
