package com.example.demo.repository;

import com.example.demo.domain.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {

	int insert(User user);

	User findByUserId(@Param("userId") String userId);

	List<User> findAll();

	int update(User user);

	int deleteByUserId(@Param("userId") String userId);

	int countByUserId(@Param("userId") String userId);
}
