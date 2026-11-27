package br.com.gestaoobras.api.exception;

import br.com.gestaoobras.api.controller.ObraController;
import br.com.gestaoobras.api.service.LancamentoFinanceiroService;
import br.com.gestaoobras.api.service.ObraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ObraController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObraService obraService;

    @MockitoBean
    private LancamentoFinanceiroService lancamentoService;

    @Test
    void deveRetornar400QuandoJsonMalformadoOuEnumInvalido() throws Exception {
        String jsonComEnumInvalido = """
                {
                  "nome": "Obra X",
                  "endereco": "Rua 1",
                  "responsavel": "Eng. A",
                  "orcamentoPrevisto": 1000,
                  "dataInicio": "2026-01-01",
                  "previsaoFim": "2026-12-01",
                  "status": "STATUS_INEXISTENTE"
                }
                """;

        mockMvc.perform(post("/obras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComEnumInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("malformado")));
    }

    @Test
    void deveRetornar400QuandoValidacaoDeCamposFalhar() throws Exception {
        String jsonComCamposInvalidos = """
                {
                  "nome": "",
                  "endereco": "",
                  "responsavel": "",
                  "orcamentoPrevisto": -10.00,
                  "dataInicio": "2026-01-01",
                  "previsaoFim": "2026-12-01"
                }
                """;

        mockMvc.perform(post("/obras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComCamposInvalidos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Erro de validação nos dados enviados.")))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.length()", greaterThan(0)));
    }

    @Test
    void deveRetornar404QuandoResourceNotFound() throws Exception {
        when(obraService.buscarDetalhesPorId(404L))
                .thenThrow(new ResourceNotFoundException("Obra não encontrada com ID: 404"));

        mockMvc.perform(get("/obras/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Obra não encontrada com ID: 404")));
    }

    @Test
    void deveRetornar409QuandoConflictOuDataIntegrityViolation() throws Exception {
        doThrow(new ConflictException("Conflito de exclusão"))
                .when(obraService).excluir(1L);

        mockMvc.perform(delete("/obras/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Conflito de exclusão")));

        doThrow(new DataIntegrityViolationException("fk_violation"))
                .when(obraService).excluir(2L);

        mockMvc.perform(delete("/obras/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void deveRetornar500SemExporStackTraceOuMensagemInterna() throws Exception {
        when(obraService.buscarDetalhesPorId(500L))
                .thenThrow(new RuntimeException("NullPointerException detalhada com segredo interno"));

        mockMvc.perform(get("/obras/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("Ocorreu um erro interno inesperado. Tente novamente mais tarde.")))
                .andExpect(jsonPath("$.message", not(containsString("segredo interno"))))
                .andExpect(jsonPath("$.message", not(containsString("NullPointerException"))));
    }
}
