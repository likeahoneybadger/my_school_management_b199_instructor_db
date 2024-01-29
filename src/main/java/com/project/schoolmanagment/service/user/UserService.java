package com.project.schoolmanagment.service.user;

import com.project.schoolmanagment.entity.concretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.exception.BadRequestException;
import com.project.schoolmanagment.exception.ResourceNotFoundException;
import com.project.schoolmanagment.payload.mappers.UserMapper;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.user.UserRequest;
import com.project.schoolmanagment.payload.request.user.UserRequestWithoutPassword;
import com.project.schoolmanagment.payload.response.abstracts.BaseUserResponse;
import com.project.schoolmanagment.payload.response.business.ResponseMessage;
import com.project.schoolmanagment.payload.response.user.UserResponse;
import com.project.schoolmanagment.repository.user.UserRepository;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final com.project.schoolmanagment.service.validator.UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final PageableHelper pageableHelper;
    private final MethodHelper methodHelper;


    public ResponseMessage<UserResponse> saveUser(UserRequest userRequest, String userRole) {
        //  We need a validator for unique properties.
        uniquePropertyValidator.checkDuplicate(
                userRequest.getUsername(),
                userRequest.getSsn(),
                userRequest.getPhoneNumber(),
                userRequest.getEmail()
        );
        //  we need to map DTO -> entity
        User user = userMapper.mapUserRequestToUser(userRequest);
        //  Analyse the role and set it to the entity
        if (userRole.equalsIgnoreCase(RoleType.ADMIN.getName())) {   //  if it is admin
            //  if username is admin, then we set this user builtIN -> TRUE, so it will not be deleted.
            if (Objects.equals(userRequest.getUsername(), "Admin")) {
                user.setBuiltIn(true);
            }
            //  since role infor is kept in another table,
            //  we need to have another repo and service to call the role
            user.setUserRole(userRoleService.getUserRole(RoleType.ADMIN));
        } else if (userRole.equalsIgnoreCase("Dean")) {
            user.setUserRole(userRoleService.getUserRole(RoleType.MANAGER));
        } else if (userRole.equalsIgnoreCase("ViceDean")) {
            user.setUserRole(userRoleService.getUserRole(RoleType.ASSISTANT_MANAGER));
        } else {
            throw new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_USERROLE_MESSAGE, userRole));
        }

        //  since we will not save teacher with these end-point
//        user.setIsAdvisor(false); //  we wrote it to UserMApper class and delete it from here.
        User savedUser = userRepository.save(user); //  we save the user to the database.

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .object(userMapper.mapUserToUserResponse(savedUser))
                .build();
    }

    public Page<UserResponse> getUsersByPage(int page, int size, String sort, String type, String userRole) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return userRepository.findByUserByRole(userRole, pageable)
                //map entity to response DTO
                .map(userMapper::mapUserToUserResponse);
    }

    public ResponseMessage<BaseUserResponse> getUSerById(Long userId) {
        //  need to check if user exist with these Id
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE, userId))
        );
        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_FOUND)
                .object(userMapper.mapUserToUserResponse(user))
                .httpStatus(HttpStatus.OK)
                .build();

    }

    public List<UserResponse> getUserByName(String userName) {
        return userRepository.getUserByNameContaining(userName)
                .stream()
                .map(userMapper::mapUserToUserResponse)
                .collect(Collectors.toList());

    }

    public String updateUser(UserRequestWithoutPassword userRequest, HttpServletRequest request) {
        String userName = (String) request.getAttribute("username");

        User user = userRepository.findByUsername(userName);

        //  we need to check if user is builtIn.

        methodHelper.checkBuiltIn(user);

        //  uniqueness control
        uniquePropertyValidator.checkUniqueProperties(user, userRequest);

        //  classic mappings instead of builder mappers
        user.setName(userRequest.getName());
        user.setSurname(userRequest.getSurname());
        user.setUsername(userRequest.getUsername());
        user.setBirthDay(userRequest.getBirthDay());
        user.setBirthPlace(userRequest.getBirthPlace());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setGender(userRequest.getGender());
        user.setSsn(userRequest.getSsn());

        userRepository.save(user);
        return SuccessMessages.USER_UPDATE;
    }

    public ResponseMessage<BaseUserResponse> updateAdminDeanViceDeanByAdmin(Long userId, UserRequest userRequest) {
        //  check user if really exists
        //  entity1: comes from DB
        User user = methodHelper.isUserExist(userId);
        //  check user is built in
        methodHelper.checkBuiltIn(user);

        uniquePropertyValidator.checkUniqueProperties(user, userRequest);
        //  entity2: created by mappers from DTO
        User userToSave = userMapper.mapUserRequestToUser(userRequest);
        userToSave.setId(user.getId());
        userToSave.setUserRole(user.getUserRole());
        //  entity3: return type of save operation
        User savedUser = userRepository.save(userToSave);
        //time to return BaseUserResponse DTO to controller
        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_UPDATE_MESSAGE)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.mapUserToUserResponse(savedUser))
                .build();
    }

    public String deleteUserById(Long id, HttpServletRequest httpServletRequest) {
        //  we dont need to write an exception everytime with the help of methodHelper. But be aware of injecting it beforehand!
        User user = methodHelper.isUserExist(id);

        //username of logged in person  -   this is very important for us. EG. we have a requirement like: Admin deletes Dean, Assistant Dean
        String userName = (String) httpServletRequest.getAttribute("username");

        User loggedInUser = userRepository.findByUsername(userName);

        RoleType loggedInUserRole = loggedInUser.getUserRole().getRoleType();
        RoleType deletedUserRole = user.getUserRole().getRoleType();
        if (user.getBuiltIn()) {
            //buildIn users can not be neither updated nor deleted.
            throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
            //  manager can only delete teacher/student/viceDean
        } else if (loggedInUserRole == RoleType.MANAGER) {
            if (!(deletedUserRole == RoleType.TEACHER ||
                    deletedUserRole == RoleType.STUDENT ||
                    deletedUserRole == RoleType.ASSISTANT_MANAGER)
            ) {
                throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
            }
            //  assistant manager can only delete teacher/student
        } else if (loggedInUserRole == RoleType.ASSISTANT_MANAGER) {
            if (!(deletedUserRole == RoleType.TEACHER ||
                    deletedUserRole == RoleType.STUDENT)
            ) {
                throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
            }
            userRepository.deleteById(id);
        }
        return SuccessMessages.USER_DELETE;
    }
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
