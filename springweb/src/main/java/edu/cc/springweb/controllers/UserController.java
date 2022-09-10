package edu.cc.springweb.controllers;

import java.util.List;

import edu.cc.springweb.models.User;
import edu.cc.springweb.services.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class UserController {

	@Autowired
	private IUserService userService;

	@GetMapping("/")
	public String index(Model model) {
	    
	    log.info("Ejecutando método index en controlador UserController");
	    
	    List<User> usuarios = userService.listadoUsuarios();
        model.addAttribute("usuarios", usuarios);
	    
		return "index";
	}
	
	@GetMapping("/add")
	public String add(User u) {
		log.info("Ejecutando método add en controlador UserController");
		return "formUser";
	}
	
	@PostMapping("/save")
	public String save(User u) {
		log.info("Ejecutando método save en controlador UserController");
		log.info("Usuario " + u);
		userService.guardarUsuario(u);
		
		return "redirect:/";
	}
	
	@GetMapping("/edit/{id}")
	public String edit(User u, Model model) {
		log.info("Ejecutando método edit en controlador USerController");
		log.info("Editando usuario " + u);
		User usuario = userService.findUsuario(u);
		
		model.addAttribute("user", usuario);
		return "formUser";
	}
	
	@GetMapping("/delete")
	public String delete(User u){
		log.info("Ejecutando método delete en controlador UserController");
		userService.eliminarUsuario(u);
		return "redirect:/";
	}
	
	
}