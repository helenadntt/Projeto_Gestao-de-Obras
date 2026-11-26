package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import br.com.gestaoobras.api.repository.CentroCustoRepository;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import br.com.gestaoobras.api.repository.ObraRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CentroCustoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Test
    void deveListarCentrosCustoPadraoECriarAtualizarExcluirNovo() throws Exception {
        mockMvc.perform(get("/centros-custo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))));

        CentroCustoRequestDTO novo = new CentroCustoRequestDTO(
                "Paisagismo",
                "Jardins e áreas externas",
                true
        );

        String response = mockMvc.perform(post("/centros-custo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Paisagismo")))
                .andExpect(jsonPath("$.ativo", is(true)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Duplicidade deve retornar 409 Conflict
        mockMvc.perform(post("/centros-custo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CentroCustoRequestDTO("paisagismo", "Duplicado", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Atualizar
        mockMvc.perform(put("/centros-custo/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CentroCustoRequestDTO("Paisagismo e Urbanismo", "Atualizado", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Paisagismo e Urbanismo")))
                .andExpect(jsonPath("$.ativo", is(false)));

        // Excluir
        mockMvc.perform(delete("/centros-custo/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar409AoExcluirCentroCustoComLancamentosSugerindoInativar() throws Exception {
        CentroCusto centro = centroCustoRepository.save(CentroCusto.builder()
                .nome("Centro Protegido")
                .ativo(true)
                .build());

        Obra obra = obraRepository.save(Obra.builder()
                .nome("Obra Teste Centro")
                .endereco("Rua C, 300")
                .responsavel("Eng. Ana")
                .orcamentoPrevisto(new BigDecimal("20000.00"))
                .dataInicio(LocalDate.now())
                .previsaoFim(LocalDate.now().plusDays(15))
                .status(StatusObra.PLANEJAMENTO)
                .progressoPercentual(0)
                .build());

        lancamentoRepository.save(LancamentoFinanceiro.builder()
                .obra(obra)
                .centroCusto(centro)
                .descricao("Despesa vinculada")
                .tipo(TipoLancamento.DESPESA)
                .valor(new BigDecimal("500.00"))
                .dataCompetencia(LocalDate.now())
                .statusPagamento(StatusPagamento.PENDENTE)
                .build());

        mockMvc.perform(delete("/centros-custo/{id}", centro.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("inativ")));
    }
}
