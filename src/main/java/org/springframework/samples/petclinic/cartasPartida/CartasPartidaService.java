package org.springframework.samples.petclinic.cartasPartida;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.carta.Carta;
import org.springframework.samples.petclinic.mazo.Mazo;
import org.springframework.samples.petclinic.mazo.MazoService;
import org.springframework.samples.petclinic.mazoFinal.MazoFinal;
import org.springframework.samples.petclinic.mazoFinal.MazoFinalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartasPartidaService {

    private CartasPartidaRepository cartasPartidaRepository;
    
    @Autowired
    private MazoService mazoService;

    @Autowired
    private MazoFinalService mazoFinalService;



    @Autowired
    public CartasPartidaService(CartasPartidaRepository cartasPartidaRepository) {
        this.cartasPartidaRepository = cartasPartidaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CartasPartida> findCartasPartidaById(Integer id) throws DataAccessException {
        return cartasPartidaRepository.findById(id);
    }


    public List<Integer> getMazosIdSorted(Integer partidaId){
        List<CartasPartida> cp = cartasPartidaRepository.findCartasPartidaByPartidaId(partidaId);
        
        List<Integer> res= new ArrayList<>();
        for (CartasPartida cartaPartida : cp) {
            if (cartaPartida == null) {
                break;
            }else{
                
                if (cartaPartida.getMazo()!= null && !res.contains(cartaPartida.getMazo().getId()) ) {
                    res.add(cartaPartida.getMazo().getId());
                }    
            }
        }

        return res; 
    }


    public List<CartasPartida> findCartasPartidaByPartidaId(int partidaId){
        List<CartasPartida> res = cartasPartidaRepository.findCartasPartidaByPartidaId(partidaId);
        return res;
        
    }


    public CartasPartida findCartasPartidaByCartaId(int cartaId){
        return cartasPartidaRepository.findCartasPartidaByCartaId(cartaId);
    }

    public List<Carta> findCartasByMazoIdList(int mazoId){
        return cartasPartidaRepository.findCartasByMazoIdList(mazoId);
    }

    @Transactional
    public void moverCartaInterInter(int mazoOrigenId, int mazoDestinoId, int cantidadCartas, int partidaId){
        Mazo mazoOrigen = mazoService.findMazoById(mazoOrigenId);
        Mazo mazoDest = mazoService.findMazoById(mazoDestinoId);

        List<CartasPartida> cpOrigen = cartasPartidaRepository.findCartasPartidaByMazoIdAndPartidaId(mazoOrigenId, partidaId);
        List<CartasPartida> cpDestino = cartasPartidaRepository.findCartasPartidaByMazoIdAndPartidaId(mazoDestinoId, partidaId);

        int startIndex = cpOrigen.size() - cantidadCartas;

        Collections.sort(cpOrigen, new ComparadorCartasPartidaPorPosCartaMazo());
        List<CartasPartida> cartasMovidas = cpOrigen.subList(startIndex, cpOrigen.size());

        int indexUltCarta = cpDestino.size();
        int i = 1;
        for (CartasPartida cp : cartasMovidas) {
            cp.setMazo(mazoDest);
            cp.setPosCartaMazo(indexUltCarta+i);
            i++;
            cartasPartidaRepository.save(cp);
        }

        System.out.println("probando");

    }

    @Transactional
    public void moverCartaInterFin(int mazoOrigenId, int mazoDestinoId, int partidaId){
        
        MazoFinal mazoDest = mazoFinalService.findMazoFinalById(mazoDestinoId);

        List<CartasPartida> cpOrigen = cartasPartidaRepository.findCartasPartidaByMazoIdAndPartidaId(mazoOrigenId, partidaId);
        List<CartasPartida> cpDestino = cartasPartidaRepository.findCartasPartidaByMazoFinalIdAndPartidaId(mazoDestinoId, partidaId);

        int startIndex = cpOrigen.size() - 1;

        Collections.sort(cpOrigen, new ComparadorCartasPartidaPorPosCartaMazo());
        List<CartasPartida> cartasMovidas = cpOrigen.subList(startIndex, cpOrigen.size());

        int indexUltCarta = cpDestino.size();
        int i = 1;
        for (CartasPartida cp : cartasMovidas) {
            cp.setMazo(null);
            cp.setMazoFinal(mazoDest);
            cp.setPosCartaMazo(indexUltCarta+i);
            i++;
            cartasPartidaRepository.save(cp);
        }

        System.out.println("probando");

    }

    @Transactional
    public void saveCartasPartida(CartasPartida cp) throws DataAccessException {
	    cartasPartidaRepository.save(cp);	
	}	

    public List<CartasPartida> findCartasPartidaByMazoId(Integer mazoId){
        List<CartasPartida> res = cartasPartidaRepository.findCartasPartidaByMazoId(mazoId);
        return res;
    }

    public List<CartasPartida> findCartasPartidaMazoInicialByPartidaId(Integer partidaId){
        List<CartasPartida> res = cartasPartidaRepository.findCartasPartidaMazoInicial(partidaId);
        return res;
    }

}