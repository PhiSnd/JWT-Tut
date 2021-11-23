package tech.getarrays.supportportal.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.getarrays.supportportal.domain.User;
import tech.getarrays.supportportal.domain.UserPrincipal;
import tech.getarrays.supportportal.repository.UserRepository;
import tech.getarrays.supportportal.service.UserService;

import java.util.Date;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;



    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
