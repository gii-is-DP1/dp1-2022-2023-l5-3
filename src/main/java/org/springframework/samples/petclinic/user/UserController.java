/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.jugador.Jugador;
import org.springframework.samples.petclinic.jugador.JugadorService;
import org.springframework.samples.petclinic.logros.Logros;
import org.springframework.samples.petclinic.logros.LogrosService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
public class UserController {

	private static final String VIEWS_JUGADOR_CREATE_FORM = "users/createJugadoresForm";

	private final JugadorService jugadorService;
	private final UserService userService;
	private final LogrosService logrosService;
	
	@Autowired
	public UserController(JugadorService jugadorService,UserService userService, LogrosService logrosService) {
		this.jugadorService = jugadorService;
		this.userService = userService;
		this.logrosService = logrosService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping(value = "/users/new")
	public String initCreationForm(Map<String, Object> model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {
					Jugador jugador = new Jugador();
					model.put("jugador", jugador);
					return VIEWS_JUGADOR_CREATE_FORM;
				} else {
					return "welcome";
				}
			}
		} else {
			return "welcome";
		}
		return "exception";
	}
	
	@PostMapping(value = "/users/new")
	public String processCreationForm(@Valid Jugador jugador, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_JUGADOR_CREATE_FORM;
		}
		else {
			jugador.setImage("");		
			jugador.setAllStats0();
					Logros logro1 = new Logros();
					Logros logro2 = new Logros();
					Logros logro3 = new Logros();
					List<Logros> lista = new ArrayList<>();
					lista.add(logro1);
					lista.add(logro2);
					lista.add(logro3);
					for(Logros logro:lista){
						if(lista.get(0).equals(logro)){
							logro.setName("M??quina de jugar");
							logro.setDescription("Has jugado 5 partidas");
						} else if(lista.get(1).equals(logro)){
							logro.setName("No se te da nada mal");
							logro.setDescription("Has alcanzado los 100 puntos");
						} else {
							logro.setName("??Est??s on fire!");
							logro.setDescription("Has alcanzado los 200 movimientos");
						}
						logro.setIs_unlocked(false);
						logro.setImage("");
						logro.setJugador(jugador);	
					}
					logrosService.save(lista.get(0));
					logrosService.save(lista.get(1));
					logrosService.save(lista.get(2));
					this.jugadorService.saveJugador(jugador);
			
					return "jugador/showJugador";
		}
	}
	
	@GetMapping("/users/all")
    public String showUsersList(Map<String, Object> model){
        
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {
					//List<Authorities> usuarios = authoritiesservice.findAllUsers();
					List<User> usuarios = userService.findAllUsers();
					Comparator<User> comparador= Comparator.comparing(User::getJugadorId);
					List<User> listaOrdenada = usuarios.stream().sorted(comparador).collect(Collectors.toList());
					model.put("users", listaOrdenada);
					return "users/UsersList";
				} else {
					return "welcome";
				}
			
			}
		} else {
			return "welcome";
		}

		return "welcome";
	}

	@GetMapping("/ranking")
    public String showRanking(Map<String, Object> model){
        
			List<Jugador> jugadores = jugadorService.findAllPlayer();
			
			Comparator<Jugador> comparador= Comparator.comparing(Jugador::getPartidasGanadas);
			Comparator<Jugador> comparador2= Comparator.comparing(Jugador::getNumTotalPuntos);
			Comparator<Jugador> comparador3= Comparator.comparing(Jugador::getNumTotalMovimientos);
			
			List<Jugador> ranking1 = jugadores.stream().sorted(comparador.reversed()).collect(Collectors.toList());
			List<Jugador> ranking2 = jugadores.stream().sorted(comparador2.reversed()).collect(Collectors.toList());
			List<Jugador> ranking3 = jugadores.stream().sorted(comparador3.reversed()).collect(Collectors.toList());

			model.put("jugadoresWIN", ranking1);
			model.put("jugadoresPTN", ranking2);
			model.put("jugadoresMOV", ranking3);
			
			return "ranking/rankingGeneral";
	}
}
