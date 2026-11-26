package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.dto.dashboard.DashboardResumoResponseDTO;
import br.com.gestaoobras.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Indicadores gerenciais, saldos consolidados e alertas operacionais")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Obter resumo consolidado do dashboard", description = "Retorna totalizadores de obras por status, progresso médio, receitas, despesas, saldos, distribuição de custos e alertas de atraso/orçamento.")
    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponseDTO> obterResumo() {
        return ResponseEntity.ok(dashboardService.obterResumoGeral());
    }
}
