package edu.cc.springweb.services;

import java.util.List;
import edu.cc.springweb.dao.IUserDao;
import edu.cc.springweb.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao usuarioDao;
    
    @Override
    @Transactional(readOnly = true)
    public List<User> listadoUsuarios() {
        List<User> listUsers = (List<User>) usuarioDao.findAll();
        return listUsers;
    }

    @Override
    @Transactional
    public void guardarUsuario(User usuario) {
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(User usuario) {
        usuarioDao.delete(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUsuario(User usuario) {
        return usuarioDao.findById(usuario.getId()).orElse(null);
    }
}