package com.vtlong.my_spring_boot_project.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vtlong.my_spring_boot_project.dto.request.CreateUserRequestDto;
import com.vtlong.my_spring_boot_project.dto.request.UpdateUserRequestDto;
import com.vtlong.my_spring_boot_project.dto.response.UserResponseDto;
import com.vtlong.my_spring_boot_project.exception.AppException;
import com.vtlong.my_spring_boot_project.exception.ErrorCode;
import com.vtlong.my_spring_boot_project.mapper.UserMapper;
import com.vtlong.my_spring_boot_project.model.User;
import com.vtlong.my_spring_boot_project.repository.UserRepository;

@Service
@Transactional
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        logger.info("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Found {} users", users.size());
        return userMapper.toResponseDtoList(users);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(String id) {
        logger.info("Fetching user by id: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserResponseDto userResponseDto = userMapper.toResponseDto(user);
            logger.info("User found with id: {}", id);
            return userResponseDto;
        } else {
            logger.warn("User not found with id: {}", id);
            throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
        }
    }

    public UserResponseDto create(CreateUserRequestDto createUserRequestDto) {
        logger.info("Creating new user with username: {}", createUserRequestDto.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(createUserRequestDto.getUsername())) {
            logger.error("Username already exists: {}", createUserRequestDto.getUsername());
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                    "Username already exists: " + createUserRequestDto.getUsername());
        }
        if (userRepository.existsByEmail(createUserRequestDto.getEmail())) {
            logger.error("Email already exists: {}", createUserRequestDto.getEmail());
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                    "Email already exists: " + createUserRequestDto.getEmail());
        }

        User user = userMapper.toEntity(createUserRequestDto);
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.toResponseDto(savedUser);
    }

    public UserResponseDto update(String id, UpdateUserRequestDto updateUserRequestDto) {
        logger.info("Updating user with id: {}", id);

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            userMapper.updateEntityFromRequestDto(updateUserRequestDto, user);
            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully with id: {}", id);
            return userMapper.toResponseDto(updatedUser);
        } else {
            logger.warn("User not found for update with id: {}", id);
            throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
        }
    }

    public void delete(String id) {
        logger.info("Deleting user with id: {}", id);

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
            logger.info("User deleted successfully with id: {}", id);
        } else {
            logger.warn("User not found for deletion with id: {}", id);
            throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
        }
    }
}
