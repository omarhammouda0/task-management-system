package com.taskmanagement.user.repository;

import com.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository <User, Long> {

    @Query ("select u from User u where u.status = com.taskmanagement.user.enums.UserStatus.ACTIVE")
    Page<User> getAllUsers(Pageable pageable);

    Optional<User> findByEmailIgnoreCase(String email);


    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(@Param ( "email" ) String email ,
                                                     @Param ( "id" ) Long id);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE " +
            "u.role = com.taskmanagement.user.enums.Role.ADMIN" +
            " AND u.status = com.taskmanagement.user.enums.UserStatus.ACTIVE" +
            " AND u.id <> :userId")
    boolean existsOtherAdmins(@Param("userId") Long userId);

    @Query ("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.id = :userId AND u.role = com.taskmanagement.user.enums.Role.ADMIN")

    boolean existsByIdAndRoleAdmin(@Param ( "userId" ) Long id);
}
