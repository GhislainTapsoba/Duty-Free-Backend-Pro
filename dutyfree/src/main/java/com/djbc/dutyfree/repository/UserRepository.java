package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.User;
import com.djbc.dutyfree.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.active = true")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.assignedCashRegister.id = :cashRegisterId")
    List<User> findByCashRegisterId(@Param("cashRegisterId") Long cashRegisterId);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);
}