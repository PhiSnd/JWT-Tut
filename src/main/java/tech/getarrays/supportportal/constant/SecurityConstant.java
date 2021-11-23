package tech.getarrays.supportportal.constant;

public class SecurityConstant {

    public static final long EXPIRATION_TIME = 432_000_000 ; //5 days expressed in milliseconds
    public static final String TOKEN_PREFIX= "Bearer ";   // no further checks needed  (how they got the token etc)
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED ="Token cannot be verified";
    public static final String GET_ARRAYS_LLC = "get Arrays,LLC"; //Provided by this Company
    public static final String GET_ARRAYS_ADMINISTRATION ="User Management Portal";
    public static final String AUTHORITIES =" authorities";
    public static final String FORBIDDEN_MASSAGE =" You need to log in to access this page";
    public static final String ACCESS_DENIED_MESSAGE ="You do not have permission to access this page";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String[] PUBLIC_URLS={"/user/login","/user/register","/user/resetpassword/**","/user/image/**"};

}
