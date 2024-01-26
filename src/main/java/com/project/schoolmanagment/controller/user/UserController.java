package com.project.schoolmanagment.controller.user;

import com.project.schoolmanagment.payload.request.user.UserRequest;
import com.project.schoolmanagment.payload.request.user.UserRequestWithoutPassword;
import com.project.schoolmanagment.payload.response.abstracts.BaseUserResponse;
import com.project.schoolmanagment.payload.response.business.ResponseMessage;
import com.project.schoolmanagment.payload.response.user.UserResponse;
import com.project.schoolmanagment.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor    //  for dependency injection
public class UserController {
    //@Autowired        instead of this, we used @RequiredArgsConstructor and final keyword
    private final UserService userService;

    @PostMapping("/save/{userRole}")
    public ResponseEntity<ResponseMessage<UserResponse>> saveUser(
            //  Single field validation in controller level.
            @RequestBody @Valid UserRequest userRequest,
            @PathVariable String userRole){// ResponseEntity is a row type.

        return ResponseEntity.ok(userService.saveUser(userRequest,userRole));
    }
    @GetMapping("/getAllUsersByPage/{userRole}")
    public ResponseEntity<Page<UserResponse>> getUserByPage(
            @PathVariable String userRole,
            @RequestParam (value = "page", defaultValue = "0") int page,
            @RequestParam (value = "size", defaultValue = "10") int size,
            @RequestParam (value = "sort", defaultValue = "name") String sort,
            @RequestParam (value = "type", defaultValue = "desc") String type    ){
        Page<UserResponse> userResponse = userService.getUsersByPage(page,size,sort,type,userRole);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseMessage<BaseUserResponse>getUserById(@PathVariable Long userId){
        return userService.getUSerById(userId);
    }

    @GetMapping("/getUserByName")
    public List<UserResponse>getUserByName(@RequestParam(name = "name") String userName){
        return userService.getUserByName(userName);
    }
    @PatchMapping("/updateUser")
    public ResponseEntity<String>updateUser(@RequestBody @Valid
                                            UserRequestWithoutPassword userRequestWithoutPassword,
                                            HttpServletRequest request
                                            ){
        return ResponseEntity.ok(userService.updateUser(userRequestWithoutPassword,request));
    }

    @PutMapping("/update/{userId}")
    public ResponseMessage<BaseUserResponse>updateAdminDeanViceDeanByAdmin(
            @RequestBody @Valid UserRequest userRequest,
            @PathVariable Long userId
    ){
        return userService.updateAdminDeanViceDeanByAdmin(userId,userRequest);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String>deleteUserById(@PathVariable Long id,
                                                HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(userService.deleteUserById(id,httpServletRequest));
    }
//    List list = new ArrayList();
//    List<String> list2 = new ArrayList<>();



}
