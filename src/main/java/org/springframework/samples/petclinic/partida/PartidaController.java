package org.springframework.samples.petclinic.partida;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.cartasPartida.CartasPartida;
import org.springframework.samples.petclinic.cartasPartida.CartasPartidaService;
import org.springframework.samples.petclinic.cartasPartida.ComparadorCartasPartidaPorPosCartaMazo;
import org.springframework.samples.petclinic.jugador.Jugador;
import org.springframework.samples.petclinic.jugador.JugadorService;
import org.springframework.samples.petclinic.util.Tuple3;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;



@Controller

public class PartidaController {

	@Autowired
	private PartidaService partidaService;
	
	@Autowired
	private CartasPartidaService cartasPartidaService;

	@Autowired
	private JugadorService jugadorService;

	@Autowired
	private PartidaBuilder pb;

	
	private static final String VIEW_CREATE_PARTIDA = "partidas/createOrUpdatePartidaForm";
	private static final String VIEW_LIST = "partidas/partidaListEnCurso";
	private static final String VIEW_LIST2 = "partidas/partidaListFinalizadas";
	private static final String TABLERO = "tableros/tablero";

	
// ==========================================CREAR PARTIDA===================================================
	
	@GetMapping(path="/partidas/create")
	public String initCreationForm(Map<String,Object> model){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Partida partida = new Partida();
			partida.setNumMovimientos(0);
			partida.setMomentoInicio(LocalDateTime.now());
			partida.setVictoria(false);
			
			String usuario = currentUser.getUsername();
			Jugador player = jugadorService.findJugadorByUsername(usuario);
			partida.setJugador(player);
			model.put("partida", partida);
			return VIEW_CREATE_PARTIDA;
		
		
	}
	
	@PostMapping(path="/partidas/create")
	public String processCreationForm(@Valid Partida p, BindingResult result, Map<String, Object> model) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		Jugador jugador = jugadorService.findJugadorByUsername(currentUser.getUsername());

		if (result.hasErrors()) {
			
			model.put("message", result.getAllErrors());
			return VIEW_CREATE_PARTIDA;
		} else if(partidaService.jugadorTienePartidaEnCurso(jugador)){
			return "redirect:/partidas/sinFinalizar";
		} else {
			//Para asociar la partida nueva a un jugador:
			String usuario = currentUser.getUsername();
			Jugador player = jugadorService.findJugadorByUsername(usuario);
			p.setJugador(player);
			p.setNumMovimientos(0); //PREDEFINIDO
			p.setMomentoInicio(LocalDateTime.now());
			p.setVictoria(false);
			this.partidaService.save(p);
			pb.crearMazosIntermedios(p);
			model.put("message", "Partida empezada");

			List<Integer> mazos = cartasPartidaService.getMazosIdSorted(p.getId());
			Map<Integer,List<CartasPartida>> dicc = new HashMap<>();
			
			for (Integer idMazo:mazos){
				List<CartasPartida> aux = cartasPartidaService.findCartasPartidaByMazoId(idMazo);
				dicc.put(idMazo, aux);
			}

			List<CartasPartida> mazoIni = cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(p.getId());			
			List<CartasPartida> cp = cartasPartidaService.findCartasPartidaByPartidaId(p.getId());
			
			cp.removeAll(mazoIni);


			//Bucle para mostrar solo las cartas en posición final de su mazo al iniciar partida
			for(CartasPartida carta:cp){
				if (carta.getPosCartaMazo() == dicc.get(carta.getMazo().getId()).size()){
					carta.setIsShow(true);
					cartasPartidaService.saveCartasPartida(carta);

				} else {
					carta.setIsShow(false);
					cartasPartidaService.saveCartasPartida(carta);
				}
			}
			
			model.put("mazInt1",dicc.get(mazos.get(0)));
			model.put("mazInt2",dicc.get(mazos.get(1)));
			model.put("mazInt3",dicc.get(mazos.get(2)));
			model.put("mazInt4",dicc.get(mazos.get(3)));
			model.put("mazInt5",dicc.get(mazos.get(4)));
			model.put("mazInt6",dicc.get(mazos.get(5)));
			model.put("mazInt7",dicc.get(mazos.get(6)));
			model.put("mazInicial", mazoIni);
			model.put("partidaId",p.getId());
			
			return TABLERO;
	
		}
	
	}

	@PostMapping(value="/partidas/moverCartaIni/{partidaId}")
	public String procesMoveCardIniForm(@PathVariable("partidaId") int partidaId,Map<String, Object> model) {
		if(cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(partidaId)==null){
			List<CartasPartida> mazoIni = new ArrayList<>();
			model.put("mazInicial", mazoIni);
		}else{
			cartasPartidaService.cambiaPosCartaMazoIni(partidaId);
			List<CartasPartida> mazoIni = cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(partidaId);				
			model.put("mazInicial", mazoIni);
		}
		List<Integer> listaMazos = cartasPartidaService.getMazosIdSorted(partidaId);
		List<Integer> listaMazosFinales = cartasPartidaService.getMazosFinalIdSorted(partidaId);
			
		for(int i=0;i<listaMazos.size();i++){
		List<CartasPartida>cpm =cartasPartidaService.findCartasPartidaByMazoId(listaMazos.get(i));
		model.put("mazInt"+(i+1),cpm);
		}	
		

		
		
		model.put("mazoFinalCorazones",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(0)));
		model.put("mazoFinalPicas",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(1)));	
		model.put("mazoFinalDiamantes",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(2)));
		model.put("mazoFinalTreboles",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(3)));	
		model.put("partidaId",partidaId);
		return TABLERO;

	}
		
			

	@GetMapping(value="/partidas/sinFinalizar")
	public String finalizarPartida (Map<String, Object> model){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		Jugador jugador = jugadorService.findJugadorByUsername(currentUser.getUsername());
		Collection<Partida> res = jugadorService.findPartidasByJugadorId(jugador.getUser().getJugadorId());
		for (Partida partida:res){
			if(partida.getMomentoFin() == null){
				model.put("id", partida.getId());
			}
		}
		return "partidas/partidaEnCursoIndividual";
	}



	@GetMapping(value="/partidas/gana")
	public String ganarPartida (Map<String, Object> model){
		model.put("message", "ENHORABUENA, ¡HAS GANADO LA PARTIDA!");
		return "partidas/messagePartida";
	}

	@GetMapping(value="/partidas/pierde")
	public String pierdePartida (Map<String, Object> model){
		model.put("message", "HAS PERDIDO LA PARTIDA");
		return "partidas/messagePartida";
	}

	@PostMapping(value="/partidas/moverCarta/{partidaId}")
	public String procesMoveCardForm(@PathVariable("partidaId") int partidaId,@RequestParam Integer mazoOrigen,@RequestParam Integer mazoDestino, @RequestParam Integer cantidad,Map<String, Object> model) {
		
			
			System.out.println(mazoOrigen);
			System.out.println(mazoDestino);
			System.out.println(cantidad);
			List<Integer> listaMazos = cartasPartidaService.getMazosIdSorted(partidaId);
			List<Integer> listaMazosFinales = cartasPartidaService.getMazosFinalIdSorted(partidaId);
			
			if (cartasPartidaService.validacionMovimiento(mazoOrigen, mazoDestino, cantidad, partidaId)){
				
				model.put("message","Movimiento hecho");
				Tuple3 mazos = cartasPartidaService.moverCartas(mazoOrigen, mazoDestino, cantidad, partidaId);
				if(cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(partidaId)==null){
					List<CartasPartida> mazoIni = new ArrayList<>();
					model.put("mazInicial", mazoIni);
				}else{
					List<CartasPartida> mazoIni = cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(partidaId);			
					model.put("mazInicial", mazoIni);
				}
	
			
				model.put("mazInt1",mazos.getFirst().get(listaMazos.get(0)));
				model.put("mazInt2",mazos.getFirst().get(listaMazos.get(1)));
				model.put("mazInt3",mazos.getFirst().get(listaMazos.get(2)));
				model.put("mazInt4",mazos.getFirst().get(listaMazos.get(3)));
				model.put("mazInt5",mazos.getFirst().get(listaMazos.get(4)));
				model.put("mazInt6",mazos.getFirst().get(listaMazos.get(5)));
				model.put("mazInt7",mazos.getFirst().get(listaMazos.get(6)));
				
				Collections.sort(mazos.getSecond().get(listaMazosFinales.get(0)), new ComparadorCartasPartidaPorPosCartaMazo());
				Collections.sort(mazos.getSecond().get(listaMazosFinales.get(1)), new ComparadorCartasPartidaPorPosCartaMazo());
				Collections.sort(mazos.getSecond().get(listaMazosFinales.get(2)), new ComparadorCartasPartidaPorPosCartaMazo());
				Collections.sort(mazos.getSecond().get(listaMazosFinales.get(3)), new ComparadorCartasPartidaPorPosCartaMazo());

				model.put("mazoFinalCorazones",mazos.getSecond().get(listaMazosFinales.get(0)));
				model.put("mazoFinalPicas",mazos.getSecond().get(listaMazosFinales.get(1)));	
				model.put("mazoFinalDiamantes",mazos.getSecond().get(listaMazosFinales.get(2)));
				model.put("mazoFinalTreboles",mazos.getSecond().get(listaMazosFinales.get(3)));
	
	 
				
				//model.put("mazInicial", mazos.getThird().get(partidaId));
				model.put("partidaId",partidaId);
				return TABLERO;
			} else {
				
				if(cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(partidaId)==null){
					List<CartasPartida> mazoIni = new ArrayList<>();
					model.put("mazInicial", mazoIni);
				}else{
					List<CartasPartida> mazoIni = cartasPartidaService.findCartasPartidaMazoInicialByPartidaId(partidaId);			
					model.put("mazInicial", mazoIni);
				}

				model.put("message","No se puede realizar ese movimiento.");

				for(int i=0;i<listaMazos.size();i++){
				List<CartasPartida>cpm =cartasPartidaService.findCartasPartidaByMazoId(listaMazos.get(i));
				model.put("mazInt"+(i+1),cpm);
				}

				Collections.sort(cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(0)), new ComparadorCartasPartidaPorPosCartaMazo());
				Collections.sort(cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(1)), new ComparadorCartasPartidaPorPosCartaMazo());
				Collections.sort(cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(2)), new ComparadorCartasPartidaPorPosCartaMazo());
				Collections.sort(cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(3)), new ComparadorCartasPartidaPorPosCartaMazo());

				model.put("mazoFinalCorazones",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(0)));
				model.put("mazoFinalPicas",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(1)));
				model.put("mazoFinalDiamantes",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(2)));
				model.put("mazoFinalTreboles",cartasPartidaService.findCartasPartidaByMazoFinalId(listaMazosFinales.get(3)));
				
				model.put("partidaId",partidaId);
				return TABLERO;
			}

			
			
			

			}
	
	
	

//===============================LISTAR PARTIDAS ================================
	
@GetMapping(value = { "/partidas/enCurso" })
	public String showPartidaList(Map<String, Object> model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {
					List<Partida> partidas = new ArrayList<>();
					partidas = (List<Partida>) partidaService.findPartidasEnCurso();
					model.put("partidas", partidas);
					return VIEW_LIST;
				} else {
					return "redirect:/";
				}
			
			}
		} else {
			return "redirect:/";
		}

		return "redirect:/";
	}



	@GetMapping(value = { "/partidas/finalizadas" })
	public String showPartidaListFin(Map<String, Object> model) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {
					List<Partida> partidas = new ArrayList<>();
					partidas = (List<Partida>) partidaService.findPartidasFinalizadas();
					model.put("partidas", partidas);
					return VIEW_LIST2;
				} else {
					return "redirect:/";
				}
			
			}
		} else {
			return "redirect:/";
		}

		return "redirect:/";
	}

	@GetMapping(value = { "/partidas/jugador" })
	public String showPartidaListViewJugador(Map<String, Object> model) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
					String credencial = usuarioR.getAuthority();
					Jugador player = jugadorService.findJugadorByUsername(username);
					if(currentUser.getUsername().equals(player.getUser().getUsername()) || credencial.equals("admin")){
						List<Partida> partidas = new ArrayList<>();
						partidas = (List<Partida>) partidaService.findPartidasFinalizadas();
						List<Partida> mazosInter = new ArrayList<>(); 
						for(Partida partida : partidas){
							if(partida.getJugador().getUser().getUsername().equals(username)){
								mazosInter.add(partida);
							}
						}
						model.put("partidas", mazosInter);
						return "partidas/partidaListUser";
					
					} else {
						return "redirect:/";
					}
			}
			return "redirect:/";
		} else {
			return "redirect:/";
		}
	}


	@GetMapping(value = { "/partidas/jugador/{id}" })
	public String showPartidaListViewJugador(@PathVariable("id") int id,Map<String, Object> model) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
					String credencial = usuarioR.getAuthority();
					Jugador player = jugadorService.findJugadorById(id);
					if(currentUser.getUsername().equals(player.getUser().getUsername()) || credencial.equals("admin")){
						List<Partida> partidas = new ArrayList<>();
						partidas = (List<Partida>) partidaService.findPartidasFinalizadas();
						List<Partida> mazosInter = new ArrayList<>(); 
						for(Partida partida : partidas){
							if(partida.getJugador().getId().equals(id)){
								mazosInter.add(partida);
							}
						}
						model.put("partidas", mazosInter);
						return "partidas/partidaListUser";
					
					} else {
						return "redirect:/";
					}
			}
			return "redirect:/";
		} else {
			return "redirect:/";
		}
	}


	//CREAR MÉTODO QUE FINALICE UNA PARTIDA
	@GetMapping(path = "/partidas/delete/{id}")
	public ModelAndView deletePartida(@PathVariable("id") int id, ModelMap modelMap) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {
					Partida partida = partidaService.findById(id);
					Jugador jugador = partida.getJugador();
					long diffInSeconds = ChronoUnit.SECONDS.between(partida.getMomentoInicio(), partida.getMomentoFin());
					if(partida.getVictoria() == true){
						jugador.setPartidasGanadas(jugador.getPartidasGanadas()-1);
						jugador.setNumTotalMovimientos(jugador.getNumTotalMovimientos()- (int) partida.getNumMovimientos());
						jugador.setTotalTiempoJugado(jugador.getTotalTiempoJugado().minusSeconds(diffInSeconds));
						jugador.setNumMaxMovimientosPartidaGanada((long) 0);
						jugador.setNumMinMovimientosPartidaGanada((long) 0);
						jugador.setMaxTiempoPartidaGanada("");
						jugador.setMinTiempoPartidaGanada("");
					}
					if(partida.getVictoria() == false){
						jugador.setPartidasNoGanadas(jugador.getPartidasNoGanadas()-1);
						jugador.setNumTotalMovimientos(jugador.getNumTotalMovimientos()- (int) partida.getNumMovimientos());
						jugador.setTotalTiempoJugado(jugador.getTotalTiempoJugado().minusSeconds(diffInSeconds));
					}
					/*List<Logros> logrosLista = logrosService.findById(jugador.getId());
					System.out.println(logrosLista);
					for(Logros logro : logrosLista){
						logrosService.delete(logro);
					}*/
					partidaService.deletePartida(partida);
					ModelAndView mazosInterult = new ModelAndView("redirect:/partidas/finalizadas");
					mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasFinalizadas());
					return mazosInterult;
				}
			}
		}
		return new ModelAndView("exception");
	}

	
	@GetMapping(path = "/partidas/finish/{id}")
	public ModelAndView finishPartida(@PathVariable("id") int id, ModelMap modelMap) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {  //SI EmazosInter ADMIN PUEDES FINALIZAR CUALQUIER PARTIDA	
					establecerFinPartidaManual(id);
					ModelAndView mazosInterult = new ModelAndView("redirect:/partidas/pierde");
					mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasEnCurso());
					return mazosInterult;
					
				} else { //SI EmazosInter JUGADOR PUEDES FINALIZAR SOLO TU PARTIDA	
					Partida partida = partidaService.findById(id);
					if(partida.getJugador().getUser().getUsername().equals(currentUser.getUsername())){
						establecerFinPartidaManual(id);
						ModelAndView mazosInterult = new ModelAndView("redirect:/partidas/pierde");
						mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasFinalizadas());
						return mazosInterult;
					} else {
						ModelAndView mazosInterult = new ModelAndView("redirect:/");
						mazosInterult.addObject("message", "No puedes finalizar esta partida");
						mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasFinalizadas());
						return mazosInterult;
					}
					
				}
			}
			
		} else {
			return new ModelAndView("exception");
		}
		return new ModelAndView("exception");
	}

	@GetMapping(path = "/partidas/finish2/{id}")
	public ModelAndView finishPartida2(@PathVariable("id") int id, ModelMap modelMap) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null){
			org.springframework.security.core.userdetails.User currentUser =  (org.springframework.security.core.userdetails.User) auth.getPrincipal();
			Collection<GrantedAuthority> usuario = currentUser.getAuthorities();
			for (GrantedAuthority usuarioR : usuario){
				String credencial = usuarioR.getAuthority();
				if (credencial.equals("admin")) {  //SI EmazosInter ADMIN PUEDES FINALIZAR CUALQUIER PARTIDA	
					establecerFinPartidaManual2(id);
					ModelAndView mazosInterult = new ModelAndView("redirect:/partidas/gana");
					mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasEnCurso());
					return mazosInterult;
					
				} else { //SI EmazosInter JUGADOR PUEDES FINALIZAR SOLO TU PARTIDA	
					Partida partida = partidaService.findById(id);
					if(partida.getJugador().getUser().getUsername().equals(currentUser.getUsername())){
						establecerFinPartidaManual2(id);
						ModelAndView mazosInterult = new ModelAndView("redirect:/partidas/gana");
						mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasFinalizadas());
						return mazosInterult;
					} else {
						ModelAndView mazosInterult = new ModelAndView("redirect:/");
						mazosInterult.addObject("message", "No puedes finalizar esta partida");
						mazosInterult.addObject("partidas", (List<Partida>) partidaService.findPartidasFinalizadas());
						return mazosInterult;
					}
					
				}
			}
			
		} else {
			return new ModelAndView("exception");
		}
		return new ModelAndView("exception");
	}

	
	
	
	//PARA ESTADÍSTICAS
	//ESTO FUNCIONA PERO SI ELIMINAMOS LAS PARTIDAS DE LA BASE DE DATOS, NO SE ACTUALIZAN LOS VALOmazosInter
	//DEBERÍAMOS PODER USAR ALGÚN TRIGGER QUE HAGA LA ACTUALIZACIÓN SOLA DE DATOS 
	
	public void establecerFinPartidaManual (Integer id){
		Partida partida = partidaService.findById(id);
		partida.setMomentoFin(LocalDateTime.now());
		partida.setVictoria(false);
		partida.setNumMovimientos(100);
		long diffInSeconds = ChronoUnit.SECONDS.between(partida.getMomentoInicio(), partida.getMomentoFin());
		Jugador player = partida.getJugador();//ESTO no tiene que actualizarse aqui, si no en el transcurso de la partida
		player.setNumTotalMovimientos(player.getNumTotalMovimientos()+(int) partida.getNumMovimientos());
		player.setNumTotalPuntos(player.getNumTotalPuntos()+(int) partida.puntos());
		player.setPartidasNoGanadas(player.getPartidasNoGanadas()+1);
		player.setTotalTiempoJugado(player.getTotalTiempoJugado().plusSeconds(diffInSeconds));
		//player.setMinTiempoPartidaGanada(null);
		//player.setMaxTiempoPartidaGanada(null);
	}

	public void establecerFinPartidaManual2(Integer id){
		Partida partida = partidaService.findById(id);
		partida.setMomentoFin(LocalDateTime.now());
		partida.setVictoria(true);
		partida.setNumMovimientos(100);
		long diffInSeconds = ChronoUnit.SECONDS.between(partida.getMomentoInicio(), partida.getMomentoFin());
		Jugador player = partida.getJugador();//ESTO no tiene que actualizarse aqui, si no en el transcurso de la partida
		player.setNumTotalMovimientos(player.getNumTotalMovimientos()+(int) partida.getNumMovimientos());
		player.setNumTotalPuntos(player.getNumTotalPuntos()+(int) partida.puntos());
		player.setPartidasGanadas(player.getPartidasGanadas()+1);
		player.setTotalTiempoJugado(player.getTotalTiempoJugado().plusSeconds(diffInSeconds));
		//player.setMinTiempoPartidaGanada(null);
		//player.setMaxTiempoPartidaGanada(null);
	}
}
	

