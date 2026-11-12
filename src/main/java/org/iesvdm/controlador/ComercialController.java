package org.iesvdm.controlador;

import java.util.List;

import org.iesvdm.modelo.Comercial;
import org.iesvdm.servicios.ComercialService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ComercialController {

	private final ComercialService comercialService;

	public ComercialController(ComercialService comercialService) {
		this.comercialService = comercialService;
	}

	@GetMapping("/comerciales")
	public String listar(Model model) {
		List<Comercial> comerciales = comercialService.listAll();
		model.addAttribute("comerciales", comerciales);
		return "comerciales";
	}

	@GetMapping("/comerciales/{id}")
	public String detalle(Model model, @PathVariable int id) {
		return comercialService.find(id)
				.map(comercial -> {
					model.addAttribute("comercial", comercial);
					model.addAttribute("pedidos", comercialService.listPedidosByComercial(id));
					return "detalle-comercial";
				})
				.orElse("redirect:/comerciales");
	}

	@GetMapping("/comerciales/crear")
	public String crear(Model model) {
		model.addAttribute("comercial", new Comercial());
		return "crear-comercial";
	}

	@PostMapping("/comerciales/crear")
	public RedirectView submitCrear(@ModelAttribute("comercial") Comercial comercial) {
		comercialService.create(comercial);
		return new RedirectView("/comerciales");
	}

	@GetMapping("/comerciales/editar/{id}")
	public String editar(Model model, @PathVariable int id) {
		return comercialService.find(id)
				.map(comercial -> {
					model.addAttribute("comercial", comercial);
					return "editar-comercial";
				})
				.orElse("redirect:/comerciales");
	}

	@PostMapping("/comerciales/editar/{id}")
	public RedirectView submitEditar(@PathVariable int id, @ModelAttribute("comercial") Comercial comercial) {
		comercial.setId(id);
		comercialService.update(comercial);
		return new RedirectView("/comerciales");
	}

	@PostMapping("/comerciales/borrar/{id}")
	public String submitBorrar(@PathVariable int id, RedirectAttributes redirectAttributes) {
		try {
			comercialService.delete(id);
			return "redirect:/comerciales";
		} catch (DataIntegrityViolationException exception) {
			redirectAttributes.addFlashAttribute("error",
					"No se puede eliminar el comercial porque tiene pedidos asociados.");
			return "redirect:/comerciales";
		}
	}
}