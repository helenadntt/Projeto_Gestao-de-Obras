package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoPagamentoUpdateDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoRequestDTO;
import br.com.gestaoobras.api.repository.CentroCustoRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LancamentoFinanceiroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @Autowired
    private LancamentoFinanceiroRepository lancamentoRepository;

    private Obra obra;
    private CentroCusto centroCusto;

    @BeforeEach
    void setup() {
        lancamentoRepository.deleteAll();
        obraRepository.deleteAll();

        obra = obraRepository.save(Obra.builder()
                .nome("Galpão Industrial Alpha")
                .cliente("Logística Sul")
                .endereco("Rodovia BR-101, km 42")
                .responsavel("Eng. Roberto Alves")
                .orcamentoPrevisto(new BigDecimal("100000.00"))
                .dataInicio(LocalDate.now().minusDays(20))
                .previsaoFim(LocalDate.now().plusDays(60))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(40)
                .build());

        centroCusto = centroCustoRepository.findAllByAtivoTrueOrderByNomeAsc().getFirst();
    }

    @Test
    void deveGerenciarLancamentosERefletirNoResumoFinanceiroEDashboard() throws Exception {
        // 1. Receita paga de R$ 80.000
        LancamentoRequestDTO receita = new LancamentoRequestDTO(
                obra.getId(),
                centroCusto.getId(),
                "Primeira medição aprovada",
                TipoLancamento.RECEITA,
                new BigDecimal("80000.00"),
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(4),
                StatusPagamento.PAGO,
                null,
                "Transferência bancária"
        );

        mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receita)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.obraNome", is("Galpão Industrial Alpha")))
                .andExpect(jsonPath("$.statusPagamento", is("PAGO")));

        // 2. Despesa pendente de R$ 25.000
        LancamentoRequestDTO despesa = new LancamentoRequestDTO(
                obra.getId(),
                centroCusto.getId(),
                "Compra de aço estrutural",
                TipoLancamento.DESPESA,
                new BigDecimal("25000.00"),
                LocalDate.now().minusDays(2),
                null,
                StatusPagamento.PENDENTE,
                "https://docs.exemplo.com/nf-99.pdf",
                null
        );

        String despesaJson = mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusPagamento", is("PENDENTE")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long despesaId = objectMapper.readTree(despesaJson).get("id").asLong();

        // 3. Registrar pagamento via PATCH
        mockMvc.perform(patch("/lancamentos/{id}/pagamento", despesaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LancamentoPagamentoUpdateDTO(StatusPagamento.PAGO, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento", is("PAGO")))
                .andExpect(jsonPath("$.dataPagamento", notNullValue()));

        // 4. Listar lançamentos da obra pelo endpoint aninhado
        mockMvc.perform(get("/obras/{id}/lancamentos", obra.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // 5. Resumo financeiro da obra
        mockMvc.perform(get("/obras/{id}/resumo-financeiro", obra.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitasPagas", is(80000.00)))
                .andExpect(jsonPath("$.totalDespesasPagas", is(25000.00)))
                .andExpect(jsonPath("$.saldoRealizado", is(55000.00)))
                .andExpect(jsonPath("$.percentualOrcamentoUtilizado", is(25.00)))
                .andExpect(jsonPath("$.orcamentoExcedido", is(false)))
                .andExpect(jsonPath("$.despesasPorCentroCusto", hasSize(1)));

        // 6. Dashboard consolidado
        mockMvc.perform(get("/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalObras", is(1)))
                .andExpect(jsonPath("$.obrasPorStatus.EM_ANDAMENTO", is(1)))
                .andExpect(jsonPath("$.saldoRealizadoGeral", is(55000.00)))
                .andExpect(jsonPath("$.progressoMedioObras", is(40.00)))
                .andExpect(jsonPath("$.alertasObras", hasSize(0)));

        // 7. Excluir despesa
        mockMvc.perform(delete("/lancamentos/{id}", despesaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveGerarAlertaNoDashboardQuandoObraEstiverAtrasadaOuComOrcamentoExcedido() throws Exception {
        Obra obraCritica = obraRepository.save(Obra.builder()
                .nome("Reforma Escola Municipal")
                .endereco("Av. Central, 100")
                .responsavel("Eng. Paula Mendes")
                .orcamentoPrevisto(new BigDecimal("10000.00"))
                .dataInicio(LocalDate.now().minusDays(60))
                .previsaoFim(LocalDate.now().minusDays(5))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(70)
                .build());

        LancamentoRequestDTO gastoAcima = new LancamentoRequestDTO(
                obraCritica.getId(),
                centroCusto.getId(),
                "Cobertura metálica",
                TipoLancamento.DESPESA,
                new BigDecimal("12500.00"),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                StatusPagamento.PAGO,
                null,
                null
        );

        mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gastoAcima)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasObras", hasSize(1)))
                .andExpect(jsonPath("$.alertasObras[0].obraNome", is("Reforma Escola Municipal")))
                .andExpect(jsonPath("$.alertasObras[0].atrasada", is(true)))
                .andExpect(jsonPath("$.alertasObras[0].orcamentoExcedido", is(true)))
                .andExpect(jsonPath("$.alertasObras[0].percentualOrcamentoUtilizado", is(125.00)));
    }

    @Test
    void deveRetornar409AoCriarOuEditarLancamentoEmObraCanceladaOuCentroInativo() throws Exception {
        Obra obraCancelada = obraRepository.save(Obra.builder()
                .nome("Obra Cancelada")
                .endereco("Rua D, 400")
                .responsavel("Eng. Marcos")
                .orcamentoPrevisto(new BigDecimal("30000.00"))
                .dataInicio(LocalDate.now().minusDays(10))
                .previsaoFim(LocalDate.now().plusDays(20))
                .status(StatusObra.CANCELADA)
                .progressoPercentual(0)
                .build());

        CentroCusto centroInativo = centroCustoRepository.save(CentroCusto.builder()
                .nome("Centro Inativo Teste")
                .ativo(false)
                .build());

        // Criar lançamento em obra cancelada -> 409
        LancamentoRequestDTO emObraCancelada = new LancamentoRequestDTO(
                obraCancelada.getId(),
                centroCusto.getId(),
                "Tentativa em obra cancelada",
                TipoLancamento.DESPESA,
                new BigDecimal("100.00"),
                LocalDate.now(),
                null,
                StatusPagamento.PENDENTE,
                null,
                null
        );

        mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emObraCancelada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Criar lançamento em centro inativo -> 409
        LancamentoRequestDTO emCentroInativo = new LancamentoRequestDTO(
                obra.getId(),
                centroInativo.getId(),
                "Tentativa em centro inativo",
                TipoLancamento.DESPESA,
                new BigDecimal("100.00"),
                LocalDate.now(),
                null,
                StatusPagamento.PENDENTE,
                null,
                null
        );

        mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emCentroInativo)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Criar lançamento válido e tentar editar movendo para obra cancelada -> 409
        LancamentoRequestDTO valido = new LancamentoRequestDTO(
                obra.getId(),
                centroCusto.getId(),
                "Lançamento válido",
                TipoLancamento.DESPESA,
                new BigDecimal("500.00"),
                LocalDate.now(),
                null,
                StatusPagamento.PENDENTE,
                null,
                null
        );

        String criadoJson = mockMvc.perform(post("/lancamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valido)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long lancamentoId = objectMapper.readTree(criadoJson).get("id").asLong();

        mockMvc.perform(put("/lancamentos/{id}", lancamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emObraCancelada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        mockMvc.perform(put("/lancamentos/{id}", lancamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emCentroInativo)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }
}
