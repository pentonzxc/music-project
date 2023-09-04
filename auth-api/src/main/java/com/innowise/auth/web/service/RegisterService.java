package com.innowise.auth.web.service;

import com.innowise.auth.domain.model.User;
import com.innowise.auth.security.exception.UserAlreadyExistException;
import com.innowise.auth.domain.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    final UserRepository userRepository;

    final PasswordEncoder passwordEncoder;

    public void register(User user) throws UserAlreadyExistException {
        Optional<User> userOpt = userRepository.findUserByUsername(user.getUsername());
        if(userOpt.isPresent()) {
            throw new UserAlreadyExistException(user.getUsername());
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }
}
