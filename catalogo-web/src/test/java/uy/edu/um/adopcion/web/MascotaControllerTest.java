package uy.edu.um.adopcion.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Sexo;
import uy.edu.um.adopcion.registro.servicio.RegistroMascotaService;
import uy.edu.um.adopcion.web.formulario.MascotaForm;
import uy.edu.um.adopcion.web.servicio.CatalogoMascotasService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MascotaControllerTest {

    @Mock
    private RegistroMascotaService registroMascotaService;

    @Mock
    private CatalogoMascotasService catalogoMascotasService;

    private MascotaController controller;

    @BeforeEach
    void setUp() {
        controller = new MascotaController(registroMascotaService, catalogoMascotasService);
    }

    @Test
    void muestraCatalogoConMascotasDelServicio() {
        var mascotas = List.of(new MascotaForm().aMascota());
        when(catalogoMascotasService.listar()).thenReturn(mascotas);
        var model = new ExtendedModelMap();

        String vista = controller.mostrarCatalogo(model);

        assertEquals("mascotas/catalogo", vista);
        assertSame(mascotas, model.get("mascotas"));
    }

    @Test
    void registraFormularioValidoYRedirigeAlCatalogo() {
        MascotaForm form = formularioValido();
        var errores = new BeanPropertyBindingResult(form, "mascotaForm");

        String vista = controller.registrar(form, errores, new ExtendedModelMap());

        assertEquals("redirect:/mascotas?registrada", vista);
        verify(registroMascotaService).registrar(any());
    }

    @Test
    void noRegistraCuandoElFormularioTieneErrores() {
        MascotaForm form = formularioValido();
        var errores = new BeanPropertyBindingResult(form, "mascotaForm");
        errores.rejectValue("fotoUrl", "invalida", "La URL es inválida");
        var model = new ExtendedModelMap();

        String vista = controller.registrar(form, errores, model);

        assertEquals("mascotas/formulario", vista);
        verify(registroMascotaService, never()).registrar(any());
    }

    private MascotaForm formularioValido() {
        MascotaForm form = new MascotaForm();
        form.setNombre("Luna");
        form.setEspecie(Especie.PERRO);
        form.setSexo(Sexo.HEMBRA);
        form.setEdadMeses(12);
        form.setFotoUrl("https://ejemplo.test/luna");
        return form;
    }
}
