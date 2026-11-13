package org.iesvdm.controlador;

import java.time.LocalDate;
import java.util.List;

import org.iesvdm.modelo.Cliente;
import org.iesvdm.servicios.ClienteService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes")
    public String listar(@RequestParam(required = false) String ciudad, Model model) {
        List<Cliente> clientes = clienteService.listByCity(ciudad);
        model.addAttribute("clientes", clientes);
        model.addAttribute("ciudad", ciudad == null ? "" : ciudad);
        return "clientes";
    }

    @GetMapping("/clientes/ciudad/{ciudad}")
    public String listarPorCiudad(@PathVariable String ciudad, Model model) {
        return listar(ciudad, model);
    }

    @GetMapping("/clientes/{id}")
    public String detalle(Model model, @PathVariable int id) {
        return clienteService.find(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    model.addAttribute("pedidos", clienteService.listPedidosByCliente(id));
                    return "detalle-cliente";
                })
                .orElse("redirect:/clientes");
    }

    @GetMapping("/clientes/crear")
    public String crear(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "crear-cliente";
    }

    @PostMapping("/clientes/crear")
    public RedirectView submitCrear(@ModelAttribute("cliente") Cliente cliente) {
        clienteService.create(cliente);
        return new RedirectView("/clientes");
    }

    @GetMapping("/clientes/editar/{id}")
    public String editar(Model model, @PathVariable int id) {
        return clienteService.find(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    model.addAttribute("comercialesDisponibles", clienteService.listComercialesDisponibles(id));
                    model.addAttribute("comercialesVinculados", clienteService.listComercialesByCliente(id));
                    model.addAttribute("fechaVinculo", LocalDate.now());
                    return "editar-cliente";
                })
                .orElse("redirect:/clientes");
    }

    @PostMapping("/clientes/editar/{id}/comerciales")
    public String submitAnadirComercial(@PathVariable int id,
            @RequestParam int comercialId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam int prioridad,
            RedirectAttributes redirectAttributes) {
        try {
            clienteService.addComercialToCliente(id, comercialId, fecha, prioridad);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error", "Ese comercial ya está vinculado a este cliente.");
        }
        return "redirect:/clientes/editar/" + id;
    }

    @PostMapping("/clientes/editar/{clienteId}/comerciales/{comercialId}/borrar")
    public String submitBorrarComercial(@PathVariable int clienteId, @PathVariable int comercialId) {
        clienteService.removeComercialFromCliente(clienteId, comercialId);
        return "redirect:/clientes/editar/" + clienteId;
    }

    @PostMapping("/clientes/editar/{id}")
    public RedirectView submitEditar(@PathVariable int id, @ModelAttribute("cliente") Cliente cliente) {
        cliente.setId(id);
        clienteService.update(cliente);
        return new RedirectView("/clientes");
    }

    @PostMapping("/clientes/borrar/{id}")
    public String submitBorrar(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.delete(id);
            return "redirect:/clientes";
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar el cliente porque tiene pedidos asociados.");
            return "redirect:/clientes";
        }
    }
}
