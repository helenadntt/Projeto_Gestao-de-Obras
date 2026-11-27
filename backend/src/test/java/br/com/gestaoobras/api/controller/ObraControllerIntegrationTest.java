package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.dto.obra.ObraStatusUpdateDTO;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import br.com.gestaoobras.api.repository.ObraRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObraControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Autowired
    private ObraRepository obraRepository;

    @BeforeEach
    void cleanDatabase() {
        lancamentoRepository.deleteAll();
        obraRepository.deleteAll();
    }

    @Test
    void deveExecutarCicloCompletoDeCrudEStatusDeObra() throws Exception {
        ObraRequestDTO novaObra = new ObraRequestDTO(
                "Residencial Solar",
                "Torre residencial com 8 andares",
                "Grupo Horizonte",
                "Rua das Palmeiras, 400",
                "Eng. Marina Costa",
                new BigDecimal("500000.00"),
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(90),
                null,
                StatusObra.EM_ANDAMENTO,
                35
        );

        String responseJson = mockMvc.perform(post("/obras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaObra)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Residencial Solar")))
                .andExpect(jsonPath("$.status", is("EM_ANDAMENTO")))
                .andExpect(jsonPath("$.progressoPercentual", is(35)))
                .andExpect(jsonPath("$.atrasada", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long obraId = objectMapper.readTree(responseJson).get("id").asLong();

        // Listar com filtro por status e busca
        mockMvc.perform(get("/obras")
                        .param("status", "EM_ANDAMENTO")
                        .param("busca", "horizonte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));

        // Visão detalhada
        mockMvc.perform(get("/obras/{id}", obraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obra.nome", is("Residencial Solar")))
                .andExpect(jsonPath("$.resumoFinanceiro.orcamentoPrevisto", is(500000.00)))
                .andExpect(jsonPath("$.resumoFinanceiro.saldoRealizado", is(0)));

        // Atualizar obra completa
        ObraRequestDTO atualizacao = new ObraRequestDTO(
                "Residencial Solar II",
                "Torre atualizada",
                "Grupo Horizonte",
                "Rua das Palmeiras, 450",
                "Eng. Marina Costa",
                new BigDecimal("600000.00"),
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(120),
                null,
                StatusObra.EM_ANDAMENTO,
                50
        );

        mockMvc.perform(put("/obras/{id}", obraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Residencial Solar II")))
                .andExpect(jsonPath("$.progressoPercentual", is(50)));

        // Concluir obra via PATCH (deve preencher dataConclusao e progresso 100 automaticamente)
        ObraStatusUpdateDTO concluir = new ObraStatusUpdateDTO(StatusObra.CONCLUIDA, null, null);
        mockMvc.perform(patch("/obras/{id}/status", obraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(concluir)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONCLUIDA")))
                .andExpect(jsonPath("$.progressoPercentual", is(100)))
                .andExpect(jsonPath("$.dataConclusao", notNullValue()));

        // Excluir obra
        mockMvc.perform(delete("/obras/{id}", obraId))
                .andExpect(status().isNoContent());

        // Buscar excluída retorna 404
        mockMvc.perform(get("/obras/{id}", obraId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveRetornar400QuandoPrevisaoFimForAnteriorADataInicio() throws Exception {
        ObraRequestDTO invalida = new ObraRequestDTO(
                "Obra Datas Inválidas",
                null,
                null,
                "Rua A, 10",
                "João",
                new BigDecimal("10000.00"),
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 1),
                null,
                StatusObra.PLANEJAMENTO,
                0
        );

        mockMvc.perform(post("/obras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("previsaoFim")));
    }

    @Test
    void deveRetornar400QuandoPayloadInvalido() throws Exception {
        ObraRequestDTO payloadInvalido = new ObraRequestDTO(
                "",
                null,
                null,
                "",
                "",
                new BigDecimal("-50.00"),
                null,
                null,
                null,
                null,
                150
        );

        mockMvc.perform(post("/obras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    void deveRetornar409AoExcluirObraComLancamentos() throws Exception {
        Obra obraComLancamento = obraRepository.save(Obra.builder()
                .nome("Obra Protegida")
                .endereco("Rua B, 200")
                .responsavel("Eng. Lucas")
                .orcamentoPrevisto(new BigDecimal("50000.00"))
                .dataInicio(LocalDate.now().minusDays(5))
                .previsaoFim(LocalDate.now().plusDays(30))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(10)
                .build());

        lancamentoRepository.save(LancamentoFinanceiro.builder()
                .obra(obraComLancamento)
                .descricao("Compra inicial")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("1000.00"))
                .dataCompetencia(LocalDate.now())
                .statusPagamento(StatusPagamento.PENDENTE)
                .build());

        mockMvc.perform(delete("/obras/{id}", obraComLancamento.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Confirma que a obra e o lançamento continuam existindo
        mockMvc.perform(get("/obras/{id}", obraComLancamento.getId()))
                .andExpect(status().isOk());
    }
}
