package tech.getarrays.supportportal.resource;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.getarrays.supportportal.exception.ExceptionHandling;

//Handles all the requests!

@RestController
@RequestMapping(path ={"/", "/user"})
public class UserResource extends ExceptionHandling {

    @GetMapping("/home")
    public String showUser(){
        return "application works";
    }
}
