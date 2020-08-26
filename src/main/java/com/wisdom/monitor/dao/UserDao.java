package com.wisdom.monitor.dao;

import com.wisdom.monitor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


public interface UserDao extends JpaRepository<User, Long> {
    @Override
    <S extends User> S save(S entity);

    Optional<User> findById(Long id);

    Optional<User> findByName(String name);

    List<User> findAll();

    @Transactional
    void deleteByName(String name);

}
