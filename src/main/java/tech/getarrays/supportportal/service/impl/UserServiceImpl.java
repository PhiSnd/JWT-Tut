package tech.getarrays.supportportal.service.impl;

import javassist.bytecode.MethodParametersAttribute;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.getarrays.supportportal.domain.User;
import tech.getarrays.supportportal.domain.UserPrincipal;
import tech.getarrays.supportportal.enumeration.Role;
import tech.getarrays.supportportal.exception.domain.EmailExistException;
import tech.getarrays.supportportal.exception.domain.EmailNotFoundException;
import tech.getarrays.supportportal.exception.domain.UsernameExistException;
import tech.getarrays.supportportal.repository.UserRepository;
import tech.getarrays.supportportal.service.EmailService;
import tech.getarrays.supportportal.service.LoginAttemptService;
import tech.getarrays.supportportal.service.UserService;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;


import static org.apache.commons.lang3.StringUtils.EMPTY;
import static tech.getarrays.supportportal.constant.FileConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    public static final String DEFAULT_USER_IMAGE_PATH = "/user/image/profile/";
    private static final String NO_USER_FOUND_BY_EMAIL = "no user found for email: ";
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private LoginAttemptService loginAttemptService;
    private BCryptPasswordEncoder passwordEncoder;
    private EmailService emailService;



    @Autowired
    public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService,EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    public UserServiceImpl() {

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);

        if (user == null){
            LOGGER.error("User not found by username" + username);
            throw new UsernameNotFoundException("User not found by username" + username);
        }else {
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user); //save-method implemented by jpa-repository
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("returning found user by username " + username);
            return userPrincipal;
        }

    }

    private void validateLoginAttempt(User user){
        if(user.isNotLocked()){
            if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())){
                user.setNotLocked(false);
            }else{
                user.setNotLocked(true);
            }
        }else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException, MessagingException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email); //Checks if Username or Email is already taken
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        //LOGGER.info("new user password:" + password);
        emailService.sendNewPasswordEmail(firstName,password,email);
        return null;
    }

    private String getTemporaryProfileImageUrl(String username) {
        //explained in part 48
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10); //random string with letters and numbers


    }

    private String generateUserId() {
        return RandomStringUtils.random(10); //generates random string

    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UsernameExistException, EmailExistException {
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        if(StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null){
                throw new UsernameNotFoundException("No user found by username" + currentUsername);
            }

            if(userByNewUsername !=null && currentUser.getId().equals(userByNewUsername.getId())){
                throw new UsernameExistException("Username already exists");
            }

            if(userByNewEmail!=null && currentUser.getId().equals(userByNewEmail.getId())){
                throw new EmailExistException("Email already exists");
            }
            return currentUser;
        } else {
            if(userByNewUsername != null){
                throw new UsernameExistException("Username already exists");
            }

            if(userByNewEmail!=null ){
                throw new EmailExistException("Email already exists");
            }
            return null;
        }

    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException {
        validateNewUsernameAndEmail(EMPTY,username,email); //EMPTY-> because the user is new
        User user = new User();
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        saveProfileImage(user, profileImage);
        return user;
    }


    //Explained in part 74
    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if (profileImage !=null){
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize(); // gives us the user folder -> user/home/supportporatl/user/rick
            if(!Files.exists(userFolder)){
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() +DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            userRepository.save(user);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH
        + username + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException {
        User currentUser = validateNewUsernameAndEmail(currentUsername,newUsername,newEmail); //EMPTY-> because the user is new
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);

    }

    @Override
    public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if(user == null){
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(user.getFirstName(),password, user.getEmail());

    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException {
        User user = validateNewUsernameAndEmail(username, null, null);
        saveProfileImage(user, profileImage);
        return user;
    }
}
