package com.wisdom.monitor.dao;

import com.wisdom.monitor.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserDao extends JpaRepository<User, Long> {
    @Override
    <S extends User> S save(S entity);

    Optional<User> findById(Long id);

    Optional<User> findByName(String name);

    List<User> findAll();

    void deleteById(Long id);

}
