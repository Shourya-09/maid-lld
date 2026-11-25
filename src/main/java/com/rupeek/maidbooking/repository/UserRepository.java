package com.rupeek.maidbooking.repository;

import com.rupeek.maidbooking.model.user.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
}
