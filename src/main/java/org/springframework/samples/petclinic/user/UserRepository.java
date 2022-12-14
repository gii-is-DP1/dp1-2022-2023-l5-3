package org.springframework.samples.petclinic.user;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends  CrudRepository<User, String>{
	
    @Query ("SELECT u FROM User u WHERE u.username =?1")
    public User findUserByUsername(String username);

    public Optional<User>findByUsername(String username);

    public List<User> findAll();
}
