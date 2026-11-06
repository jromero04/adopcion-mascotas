package uy.edu.um.adopcion.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Sexo;
import uy.edu.um.adopcion.registro.servicio.RegistroMascotaService;
import uy.edu.um.adopcion.web.formulario.MascotaForm;
import uy.edu.um.adopcion.web.servicio.CatalogoMascotasService;

@Controller
public class MascotaController {

    private final RegistroMascotaService registroMascotaService;
    private final CatalogoMascotasService catalogoMascotasService;

    public MascotaController(
            RegistroMascotaService registroMascotaService,
            CatalogoMascotasService catalogoMascotasService
    ) {
        this.registroMascotaService = registroMascotaService;
        this.catalogoMascotasService = catalogoMascotasService;
    }

    @GetMapping("/mascotas/nueva")
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("mascotaForm")) {
            model.addAttribute("mascotaForm", new MascotaForm());
        }
        agregarOpciones(model);
        return "mascotas/formulario";
    }

    @PostMapping("/mascotas")
    public String registrar(
            @Valid @ModelAttribute("mascotaForm") MascotaForm mascotaForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            agregarOpciones(model);
            return "mascotas/formulario";
        }

        registroMascotaService.registrar(mascotaForm.aMascota());
        return "redirect:/mascotas?registrada";
    }

    @GetMapping("/mascotas")
    public String mostrarCatalogo(Model model) {
        model.addAttribute("mascotas", catalogoMascotasService.listar());
        return "mascotas/catalogo";
    }

    private void agregarOpciones(Model model) {
        model.addAttribute("especies", Especie.values());
        model.addAttribute("sexos", Sexo.values());
    }
}
