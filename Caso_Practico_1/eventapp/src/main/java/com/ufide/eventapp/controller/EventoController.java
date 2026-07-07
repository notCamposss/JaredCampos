package com.ufide.eventapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ufide.eventapp.entity.Evento;
import com.ufide.eventapp.service.EventoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("eventos", service.listar());
        return "eventos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Evento evento = service.buscarPorId(id).orElse(null);
        model.addAttribute("evento", evento);
        return "evento";
    }

    @GetMapping("/categoria")
    public String categoria(@RequestParam String categoria, Model model) {

        model.addAttribute("eventos", service.buscarPorCategoria(categoria));

        return "eventos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("evento", new Evento());

        return "form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("evento") Evento evento,
                          BindingResult result) {

        if (result.hasErrors()) {
            return "form";
        }

        service.guardar(evento);

        return "redirect:/eventos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {

        Evento evento = service.buscarPorId(id).orElse(null);

        if (evento == null) {
            return "redirect:/eventos";
        }

        model.addAttribute("evento", evento);

        return "form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("evento") Evento evento,
                             BindingResult result) {

        if (result.hasErrors()) {
            return "form";
        }

        evento.setId(id);

        service.guardar(evento);

        return "redirect:/eventos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {

        service.eliminar(id);

        return "redirect:/eventos";
    }

}