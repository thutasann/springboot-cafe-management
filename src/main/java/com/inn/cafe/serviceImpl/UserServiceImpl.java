package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUsersDetailService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailService customerUsersDetailService;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside SignUp {}", requestMap);
        try {
            if (validateSignup(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Registered successfully", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Email already exist", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity("fuck", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignup(Map<String, String> requestMap){
        if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password")){
            return true;
        }
        else{
            return false;
        }
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside Login");
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );
            if(auth.isAuthenticated()){
                if(customerUsersDetailService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String>("{\"token\":\""+
                            jwtUtil.generateToken(customerUsersDetailService.getUserDetail().getEmail(),
                                    customerUsersDetailService.getUserDetail().getRole()) + "\"}", HttpStatus.OK);
                }
                else{
                    return new ResponseEntity<String>("{\"message\":\""+"Wait for admin approval."+ "\"}" , HttpStatus.BAD_REQUEST);
                }
            }
        }
        catch (Exception exception){
            log.error("{}", exception);
        }
        return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}", HttpStatus.BAD_REQUEST);
    }
}
