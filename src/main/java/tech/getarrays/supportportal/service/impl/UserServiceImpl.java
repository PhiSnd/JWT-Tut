package tech.getarrays.supportportal.service.impl;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.getarrays.supportportal.domain.User;
import tech.getarrays.supportportal.domain.UserPrincipal;
import tech.getarrays.supportportal.enumeration.Role;
import tech.getarrays.supportportal.exception.domain.EmailExistException;
import tech.getarrays.supportportal.exception.domain.UsernameExistException;
import tech.getarrays.supportportal.repository.UserRepository;
import tech.getarrays.supportportal.service.UserService;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;



    @Autowired
    public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user); //save-method implemented by jpa-repository
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("returning found user by username " + username);
            return userPrincipal;
        }

    }

    @Override
    public User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException {
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
        user.setProfileImageUrl(getTemporaryProfileImageUrl());
        userRepository.save(user);
        LOGGER.info("new user password:" + password);
        return null;
    }

    private String getTemporaryProfileImageUrl() {
        //explained in part 48
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
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
        if(StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null){
                throw new UsernameNotFoundException("No user found by username" + currentUsername);
            }
            User userByNewUsername = findUserByUsername(newUsername);
            if(userByNewUsername !=null && currentUser.getId().equals(userByNewUsername.getId())){
                throw new UsernameExistException("Username already exists");
            }
            User userByNewEmail = findUserByEmail(newEmail);
            if(userByNewEmail!=null && currentUser.getId().equals(userByNewEmail.getId())){
                throw new EmailExistException("Username already exists");
            }
            return currentUser;
        } else {
            User userByUsername = findUserByUsername(newUsername);
            if(userByUsername != null){
                throw new UsernameExistException("Username already exists");
            }
            User userByEmail = findUserByEmail(newEmail);
            if(userByEmail!=null ){
                throw new EmailExistException("Username already exists");
            }
            return null;
        }

    }

    @Override
    public List<User> getUsers() {
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        return null;
    }
}
