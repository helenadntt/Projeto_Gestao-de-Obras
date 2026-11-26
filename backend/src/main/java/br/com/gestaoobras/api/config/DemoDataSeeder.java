package br.com.gestaoobras.api.config;

import br.com.gestaoobras.api.domain.entity.CentroCusto;
import br.com.gestaoobras.api.domain.entity.LancamentoFinanceiro;
import br.com.gestaoobras.api.domain.entity.Obra;
import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.repository.CentroCustoRepository;
import br.com.gestaoobras.api.repository.LancamentoFinanceiroRepository;
import br.com.gestaoobras.api.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Popula dados realistas de demonstração quando o perfil "demo" está ativo
 * (ex.: SPRING_PROFILES_ACTIVE=dev,demo) e a tabela de obras está vazia.
 */
@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final ObraRepository obraRepository;
    private final CentroCustoRepository centroCustoRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (obraRepository.count() > 0) {
            return;
        }

        log.info("Perfil 'demo' ativo — inserindo obras e lançamentos financeiros de demonstração...");

        Map<String, CentroCusto> centrosPorNome = centroCustoRepository.findAll()
                .stream()
                .collect(Collectors.toMap(CentroCusto::getNome, Function.identity(), (a, b) -> a));

        CentroCusto materiais = centrosPorNome.get("Materiais de Construção");
        CentroCusto maoDeObra = centrosPorNome.get("Mão de Obra");
        CentroCusto equipamentos = centrosPorNome.get("Equipamentos & Maquinário");
        CentroCusto projetos = centrosPorNome.get("Projetos & Licenças");
        CentroCusto terceirizados = centrosPorNome.get("Serviços Terceirizados");
        CentroCusto aporte = centrosPorNome.get("Aporte / Receita de Cliente");

        LocalDate hoje = LocalDate.now();

        Obra residencial = obraRepository.save(Obra.builder()
                .nome("Residencial Parque das Flores")
                .descricao("Construção de edifício residencial de 6 pavimentos com área de lazer.")
                .cliente("Construtora Horizonte Ltda")
                .endereco("Av. Brasil, 1450 - Centro")
                .responsavel("Eng. Mariana Oliveira")
                .orcamentoPrevisto(new BigDecimal("650000.00"))
                .dataInicio(hoje.minusDays(90))
                .previsaoFim(hoje.plusDays(120))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(55)
                .build());

        Obra escola = obraRepository.save(Obra.builder()
                .nome("Reforma Escola Municipal Monteiro Lobato")
                .descricao("Adequação de acessibilidade, troca de cobertura e revitalização elétrica.")
                .cliente("Prefeitura Municipal")
                .endereco("Rua das Acácias, 320 - Jardim América")
                .responsavel("Eng. Carlos Eduardo")
                .orcamentoPrevisto(new BigDecimal("180000.00"))
                .dataInicio(hoje.minusDays(110))
                .previsaoFim(hoje.minusDays(8))
                .status(StatusObra.EM_ANDAMENTO)
                .progressoPercentual(82)
                .build());

        Obra galpao = obraRepository.save(Obra.builder()
                .nome("Galpão Logístico Via Norte")
                .descricao("Estrutura pré-moldada com 1.200 m² de área útil e docas de carga.")
                .cliente("TransNorte Logística S.A.")
                .endereco("Distrito Industrial, Lote 14")
                .responsavel("Eng. Fernanda Lima")
                .orcamentoPrevisto(new BigDecimal("420000.00"))
                .dataInicio(hoje.plusDays(10))
                .previsaoFim(hoje.plusDays(210))
                .status(StatusObra.PLANEJAMENTO)
                .progressoPercentual(10)
                .build());

        Obra clinica = obraRepository.save(Obra.builder()
                .nome("Clínica Odontológica Sorriso Pleno")
                .descricao("Reforma comercial completa com 5 consultórios e recepção.")
                .cliente("Dra. Beatriz Santos")
                .endereco("Rua XV de Novembro, 88 - Sala 201")
                .responsavel("Eng. Mariana Oliveira")
                .orcamentoPrevisto(new BigDecimal("140000.00"))
                .dataInicio(hoje.minusDays(150))
                .previsaoFim(hoje.minusDays(20))
                .dataConclusao(hoje.minusDays(22))
                .status(StatusObra.CONCLUIDA)
                .progressoPercentual(100)
                .build());

        lancamentoRepository.saveAll(List.of(
                // Residencial
                LancamentoFinanceiro.builder()
                        .obra(residencial).centroCusto(aporte)
                        .descricao("Aporte inicial e 1ª medição aprovada")
                        .tipo(TipoLancamento.RECEITA).valor(new BigDecimal("320000.00"))
                        .dataCompetencia(hoje.minusDays(80)).dataPagamento(hoje.minusDays(78))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(residencial).centroCusto(aporte)
                        .descricao("2ª medição estrutural")
                        .tipo(TipoLancamento.RECEITA).valor(new BigDecimal("180000.00"))
                        .dataCompetencia(hoje.minusDays(5))
                        .statusPagamento(StatusPagamento.PENDENTE)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(residencial).centroCusto(materiais)
                        .descricao("Concreto usinado e aço CA-50")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("145000.00"))
                        .dataCompetencia(hoje.minusDays(55)).dataPagamento(hoje.minusDays(54))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(residencial).centroCusto(maoDeObra)
                        .descricao("Folha de pedreiros e armadores (etapa estrutura)")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("112000.00"))
                        .dataCompetencia(hoje.minusDays(25)).dataPagamento(hoje.minusDays(24))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(residencial).centroCusto(equipamentos)
                        .descricao("Locação de grua e betoneiras")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("28500.00"))
                        .dataCompetencia(hoje.minusDays(10))
                        .statusPagamento(StatusPagamento.PENDENTE)
                        .build(),

                // Escola (atrasada + orçamento excedido -> gera alerta no Dashboard)
                LancamentoFinanceiro.builder()
                        .obra(escola).centroCusto(aporte)
                        .descricao("Repasse convênio municipal - parcelas 1 e 2")
                        .tipo(TipoLancamento.RECEITA).valor(new BigDecimal("195000.00"))
                        .dataCompetencia(hoje.minusDays(90)).dataPagamento(hoje.minusDays(85))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(escola).centroCusto(materiais)
                        .descricao("Telhas termoacústicas e estrutura metálica")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("115000.00"))
                        .dataCompetencia(hoje.minusDays(60)).dataPagamento(hoje.minusDays(58))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(escola).centroCusto(terceirizados)
                        .descricao("Reforma completa de quadro elétrico e SPDA")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("78000.00"))
                        .dataCompetencia(hoje.minusDays(15)).dataPagamento(hoje.minusDays(12))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),

                // Galpão (planejamento)
                LancamentoFinanceiro.builder()
                        .obra(galpao).centroCusto(projetos)
                        .descricao("Projeto estrutural, sondagem SPT e alvará")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("24000.00"))
                        .dataCompetencia(hoje.minusDays(7)).dataPagamento(hoje.minusDays(6))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),

                // Clínica (concluída)
                LancamentoFinanceiro.builder()
                        .obra(clinica).centroCusto(aporte)
                        .descricao("Pagamento total contratado da reforma")
                        .tipo(TipoLancamento.RECEITA).valor(new BigDecimal("140000.00"))
                        .dataCompetencia(hoje.minusDays(30)).dataPagamento(hoje.minusDays(25))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(clinica).centroCusto(materiais)
                        .descricao("Porcelanatos, divisórias drywall e luminárias")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("68000.00"))
                        .dataCompetencia(hoje.minusDays(95)).dataPagamento(hoje.minusDays(94))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build(),
                LancamentoFinanceiro.builder()
                        .obra(clinica).centroCusto(maoDeObra)
                        .descricao("Equipe de acabamento, elétrica e marcenaria")
                        .tipo(TipoLancamento.DESPESA).valor(new BigDecimal("59500.00"))
                        .dataCompetencia(hoje.minusDays(45)).dataPagamento(hoje.minusDays(44))
                        .statusPagamento(StatusPagamento.PAGO)
                        .build()
        ));

        log.info("Dados de demonstração inseridos com sucesso (4 obras e 12 lançamentos).");
    }
}
