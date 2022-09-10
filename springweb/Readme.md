# Aplicación Spring a producción en Heroku

En esta guía vamos a detallar los pasos para desplegar una aplicación Spring MVC y JPA en producción en Heroku.
Utilizaremos AWS Cloud9 como entorno de desarrollo y PostgreSQL como SGBD.


## Instala Spring CLI

Desde la web https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started-installing-the-cli
descarga spring-boot-cli-X.X.X.RELEASE-bin.zip, donde X.X.X es la última versión disponible, en esta guía spring-boot-cli-2.6.6-bin.zip.

En AWS Cloud9, descárgalo con wget:

```
$ wget https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/2.6.6/spring-boot-cli-2.6.6-bin.zip


--2022-04-21 06:44:54--  https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/2.6.6/spring-boot-cli-2.6.6-bin.zip
Resolving repo.spring.io (repo.spring.io)... 35.243.130.159
Connecting to repo.spring.io (repo.spring.io)|35.243.130.159|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 14307754 (14M) [application/zip]
Saving to: ‘spring-boot-cli-2.6.6-bin.zip’

spring-boot-cli-2.6.6-bin.zip                   100%[====================================================================================================>]  13.64M  13.3MB/s    in 1.0s    

2022-04-21 06:44:55 (13.3 MB/s) - ‘spring-boot-cli-2.6.6-bin.zip’ saved [14307754/14307754]

```

Descomprime el contenido con:

```
$ unzip spring-boot-cli-2.6.6-bin.zip

Archive:  spring-boot-cli-2.6.6-bin.zip
   creating: spring-2.6.6/
   creating: spring-2.6.6/lib/
  inflating: spring-2.6.6/lib/spring-boot-cli-2.6.6.jar  
   creating: spring-2.6.6/shell-completion/
   creating: spring-2.6.6/shell-completion/bash/
  inflating: spring-2.6.6/shell-completion/bash/spring  
   creating: spring-2.6.6/shell-completion/zsh/
  inflating: spring-2.6.6/shell-completion/zsh/_spring  
   creating: spring-2.6.6/legal/
  inflating: spring-2.6.6/legal/open_source_licenses.txt  
   creating: spring-2.6.6/bin/
  inflating: spring-2.6.6/bin/spring.bat  
  inflating: spring-2.6.6/LICENCE.txt  
  inflating: spring-2.6.6/INSTALL.txt  
  inflating: spring-2.6.6/bin/spring  
```

Mueve el directorio 'spring-2.6.6/', por ejemplo, al directorio $HOME

```
$ mv spring-2.6.6 $HOME
```

Crea una variable de entorno SPRING_HOME cuyo valor sea el directorio de Spring CLI y 
añade a la variable de entorno PATH la ruta SPRING_HOME/bin. Por ejemplo, en AWS Cloud9 
podemos añadir al fichero `$HOME/.bashrc` el siguiente contenido:

```
export SPRING_HOME=$HOME/spring-2.6.6
export PATH=$SPRING_HOME/bin:$PATH
```

Comprueba que la instalación ha sido correcta con:

```
$ spring --version
Spring CLI v2.6.6
```


## Instalar PostgreSQL

Para ubuntu:

```
$ sudo apt update
$ sudo apt install postgresql postgresql-contrib
```

Acceder al shell de PostgreSQL, Crea una base de datos, un usuario (rol) y otorgar privilegios:

```
$ sudo -u postgres psql
psql (10.19 (Ubuntu 10.19-0ubuntu0.18.04.1))
Type "help" for help.

postgres=# create database ccdb;
postgres=# create user admin with encrypted password 'adminPass';
postgres=# grant all privileges on database ccdb to admin;
```

Algunos comandos de interés:

```
\q : Cerrar conexion (salir del interprete de PostgreSQL)
\l : lista bases de datos
\c <data_base> : Conectar a una base de datos
\du : Listar usuarios
\dt : listar tablas
\d <table> : Ver tabla

```


Para acceder de forma local, editar el fichero pg_hba.conf

```
$ sudo nano /etc/postgresql/9.5/main/pg_hba.conf
```
Cambiar el método de autenticación a 'trust' para las siguientes líneas:

```
# "local" is for Unix domain socket connections only
local   all             all                                     trust
# IPv4 local connections:
host    all             all             127.0.0.1/32            trust
```

Para redhat:
```
$ sudo yum update
$ yum list postgresql*

$ sudo yum install postgresql-server.x86_64 

$ sudo service postgresql initdb

$ sudo chkconfig postgresql on

$ sudo service postgresql start
Starting postgresql96 service:                             [  OK  ]

$ sudo service postgresql status
postmaster (pid  10313) is running...
```

Para crear la BD y configurarla utiliza los comandos anteriores.


## Crear Aplicación Spring

Implementaremos una aplicación utilizando los patrones MVC y DAO

                                      '------ Modelo -----'
    Vista  <--->  Controlador  <--->  Servicio  <--->  DAO  <---> DB

Crea una nueva aplicación utilizando Spring CLI, que incluya las dependdencias
necesarias para crear una aplicación MVC, con JPA y PostgreSQL, mediante el comando:

```
$ spring init --dependencies=lombok,web,thymeleaf,jpa,postgresql --package-name=edu.cc.examples.springdata  springweb
```


### Crear una entidad

En la carpeta src/main/java/edu/cc/springweb creamos la carpeta models y dentro el fichero User.java

```
package edu.cc.springweb.models;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="usuarios")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
    
	private String nombre;
	private String email;
	
}
```

### Crear Interface DAO - Data Access Object 

En la carpeta src/main/java/edu/cc/springweb creamos la carpeta dao  y dentro el fichero IUserDao.java


```
package edu.cc.springweb.dao;

import edu.cc.springweb.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserDao extends JpaRepository<User, Long>{

	// ya temdriamos los prinicpales métodos CRUD
	// y podemos crear nuestros propios métodos

}
```


### Crear Servicio de acceso al DAO

En la carpeta src/main/java/edu/cc/springweb creamos la carpeta services y dentro el fichero IUserService.java



```

package edu.cc.springweb.services;

import java.util.List;
import edu.cc.springweb.models.User;

public interface IUserService {

	public List<User> listadoUsuarios();
	
	public void guardarUsuario(User user);
	
	public void eliminarUsuario(User user);
	
	public User findUsuario(User user);
	
}
```

Para implementar esa interface, en la carpeta src/main/java/edu/cc/springweb/services y dentro el fichero UserServiceImpl.java

```
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
```

### Crear el controlador

En la carpeta src/main/java/edu/cc/springweb creamos la carpeta controllers y dentro el fichero UserController.java


```
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
```

### Crear vistas

En la carpeta src/main/resources/templates crearemos los siguientes ficheros:

La vista index: index.html

```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

    <head>
        <title>Index</title>
    </head>
    
    <body>
        <h1>Inicio v.1</h1>
        <p th:text="${mensaje}"></p>

        <p><a th:href="@{/add}">Añadir Usuario</a></p>
       
       <table th:if="${usuarios!=null and !usuarios.empty}">
    	<tr>
    	  <th>Nombre</th>
    	  <th>Correo</th>
    	  <th>Acciones</th>
    	</tr>
    	<tr th:each="usuario : ${usuarios}">
          <td th:text="${usuario.nombre}">Nombre Persona</td>
    	  <td th:text="${usuario.email}">Correo Persona</td>
    	  <td>
    	      <a th:href="@{/edit/} + ${usuario.id}" th:text="Editar">Editar</a> | 
    	      <a th:href="@{/delete(id=${usuario.id})}" th:text="Eliminar">Eliminar</a>
    	  </td>
    	</tr>
    	</table>
    	<p th:if="${usuarios==null or usuarios.empty}">No hay usuarios</p>	
	
	</body>
</html>

```

La vista formUser: formUser.html

```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

    <head>
        <title>Usuarios</title>
    </head>
    
    <body>
        <h1>Añadir usuario</h1>
        
        <form th:action="@{/save}" method="post" th:object="${user}">
            <input type="hidden" name="id"  th:field="*{id}" />            
            <p>Nombre: <input type="text" name="nombre" th:field="*{nombre}" /></p>
	        <p>Correo: <input type="email" name="email" th:field="*{email}" /></p>
	        <p><input type="submit" value="Enviar" /></p>
        </form>


        <p><a th:href="@{/}">Volver</a></p>
	
	</body>
</html>
```

### Configurar acceso a datos

Fichero de configutación application.properties:

```
spring.datasource.hikari.connectionTimeout=20000
spring.datasource.hikari.maximumPoolSize=5
spring.jpa.database=POSTGRESQL
spring.datasource.platform=postgres
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.show-sql=true
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
```

NOTA: Crear las variables de entorno con los valores adecuados:

```
export DB_HOST=localhost
export DB_NAME=ccdb
export DB_USERNAME=admin
export DB_PASSWORD=adminPass
```

### Compilar el proyecto

Crea el paquete .jar del proyecto
```
./mvnw clean package
```

### Ejecuta el proyecto

Lanza el proyecto con:

```
$ ./mvnw spring-boot:run
```

o

```
$ java -jar target/ficheroproyecto.jar
```

NOTA: en AWS Cloud9 podrás acceder desde una URL similar a: https://d5fb5be6358943a8922adb524bb8819f.vfs.cloud9.us-east-1.amazonaws.com


## Descargar Heroku CLI

```
$ sudo snap install --classic heroku
heroku v7.60.1 from Heroku✓ installed


$ heroku --version
heroku/7.60.1 linux-x64 node-v14.19.0

```
O bien:

```

$ npm install -g heroku

$ heroku --version
heroku/7.60.1 linux-x64 node-v14.19.0

```

## Inicia Sessión en tu cuenta de Heroku

Utilizando Heroku CLI inicia sesión en tu cuenta Heroku co

```
$ heroku login --interactive      
heroku: Enter your login credentials
Email: alvarez.uhu.es@gmail.com
Password: *******
Logged in as alvarez.uhu.es@gmail.com

heroku: Enter your login credentials
Email: correo@mail.com 
Password: *******
Logged in as correo@mail.com
```

## Crea una aplicación en heroku

```
$ heroku create
Creating app... done, safe-woodland-66362
https://safe-woodland-66362.herokuapp.com/ | https://git.heroku.com/safe-woodland-66362.git
```

NOTA: El nombre para tu aplicación será diferente. También, puedes indicarle como parámetro un nombre para tu aplicación, pero debería
ser único dentro del dominoi de Heroku.

## Crea una base de datos

```
heroku addons:create heroku-postgresql:hobby-dev -a safe-woodland-66362
```

Para obtener la información de conexión:

```
$ heroku config -a safe-woodland-66362
=== safe-woodland-66362 Config Vars
DATABASE_URL: postgres://evxybkkmiodzdd:857fa7799badf797bc9ae444b67dab94d2490e4a25d5a2d112019fbc75b8c42e@ec2-54-225-237-84.compute-1.amazonaws.com:5432/d3ftstv7m1o4k2
```
La URL se muestra en el formado  postgres://[user]:[pass]@[host]:[port]/[bd]

Crear las variables de entorno en Heroku con la siguiente sistaxis:

```
$ heroku config:set VAR=VAL -a APP
```

```
$ heroku config:set DB_USERNAME=evxybkkmiodzdd -a safe-woodland-66362
Adding config vars and restarting myapp... done, v6
DB_USERNAME: evxybkkmiodzdd

$ heroku config:set DB_PASSWORD=857fa7799badf797bc9ae444b67dab94d2490e4a25d5a2d112019fbc75b8c42e -a safe-woodland-66362
Adding config vars and restarting myapp... done, v7
DB_PASSWORD: 857fa7799badf797bc9ae444b67dab94d2490e4a25d5a2d112019fbc75b8c42e

$ heroku config:set DB_HOST=ec2-54-225-237-84.compute-1.amazonaws.com -a safe-woodland-66362
Adding config vars and restarting myapp... done, v8
DB_HOST: ec2-54-225-237-84.compute-1.amazonaws.com

$ heroku config:set DB_NAME=d3ftstv7m1o4k2 -a safe-woodland-66362
Adding config vars and restarting myapp... done, v9
DB_NAME: d3ftstv7m1o4k2

```


## Inicia un repositorio Git de la aplicación

```
$ git init

$ git add .

$ git commit -m "first commit"

$ heroku git:remote -a safe-woodland-66362

$ git remote add heroku https://git.heroku.com/safe-woodland-66362.git
```


## Despliega la aplicación a producción

```
$ git push heroku master

...

remote: Compressing source files... done.
remote: Building source:
remote:
remote: -----> Java app detected
remote: -----> Installing JDK 1.8... done
remote: -----> Executing: ./mvnw -DskipTests clean dependency:list install
...
remote:        [INFO] BUILD SUCCESS
remote:        [INFO] ------------------------------------------------------------------------
remote:        [INFO] Total time:  15.173 s
remote:        [INFO] Finished at: 2019-02-06T02:20:42Z
remote:        [INFO] ------------------------------------------------------------------------
remote: -----> Discovering process types
remote:        Procfile declares types     -> (none)
remote:        Default types for buildpack -> web
remote:
remote: -----> Compressing...
remote:        Done: 64.8M
remote: -----> Launching...
remote:        Released v3
remote:        https://safe-woodland-66362.herokuapp.com/ deployed to Heroku
remote:
remote: Verifying deploy... done.
To https://git.heroku.com/safe-woodland-66362.git
 * [new branch]      master -> master
```

Accede a la URL de a aplicación, en este caso https://safe-woodland-66362.herokuapp.com/
