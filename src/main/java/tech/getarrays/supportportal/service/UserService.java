package tech.getarrays.supportportal.service;

import org.springframework.web.multipart.MultipartFile;
import tech.getarrays.supportportal.domain.User;
import tech.getarrays.supportportal.exception.domain.EmailExistException;
import tech.getarrays.supportportal.exception.domain.EmailNotFoundException;
import tech.getarrays.supportportal.exception.domain.UsernameExistException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException, MessagingException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException;
    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException;
    void deleteUser(long id);
    void resetPassword(String email) throws MessagingException, EmailNotFoundException;
    User updateProfileImage(String username, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException;


}
