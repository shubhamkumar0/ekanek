package com.example.ekanek.Interfaces;

import com.example.ekanek.Dao.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User,Long> {
    public User findByEmail(String email);
}
