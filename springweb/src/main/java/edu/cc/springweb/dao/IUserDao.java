package edu.cc.springweb.dao;

import edu.cc.springweb.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserDao extends JpaRepository<User, Long>{

	// ya temdriamos los prinicpales métodos CRUD
	// y podemos crear nuestros propios métodos

}