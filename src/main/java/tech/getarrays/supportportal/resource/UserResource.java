package tech.getarrays.supportportal.resource;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.getarrays.supportportal.domain.User;
import tech.getarrays.supportportal.exception.ExceptionHandling;
import tech.getarrays.supportportal.exception.domain.EmailExistException;
import tech.getarrays.supportportal.exception.domain.UserNotFoundException;
import tech.getarrays.supportportal.exception.domain.UsernameExistException;
import tech.getarrays.supportportal.service.UserService;

//Handles all the requests!

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling {
    private UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, UsernameExistException {
        User newUser  =userService.register(user.getFirstName(),user.getLastName(),user.getUsername(),user.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }
}
