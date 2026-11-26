package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.enums.StatusObra;
import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoResponseDTO;
import br.com.gestaoobras.api.dto.obra.ObraDetalheResponseDTO;
import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import br.com.gestaoobras.api.dto.obra.ObraResponseDTO;
import br.com.gestaoobras.api.dto.obra.ObraStatusUpdateDTO;
import br.com.gestaoobras.api.dto.obra.ResumoFinanceiroObraDTO;
import br.com.gestaoobras.api.service.LancamentoFinanceiroService;
import br.com.gestaoobras.api.service.ObraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/obras")
@RequiredArgsConstructor
@Tag(name = "Obras", description = "Gerenciamento completo de obras, prazos, progresso e visão financeira por obra")
public class ObraController {

    private final ObraService obraService;
    private final LancamentoFinanceiroService lancamentoService;

    @Operation(summary = "Listar obras", description = "Retorna lista paginada de obras com filtros opcionais por status e busca textual (nome, cliente ou responsável).")
    @GetMapping
    public ResponseEntity<Page<ObraResponseDTO>> listar(
            @Parameter(description = "Filtrar por status da obra")
            @RequestParam(required = false) StatusObra status,
            @Parameter(description = "Busca textual em nome, cliente ou responsável")
            @RequestParam(required = false) String busca,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(obraService.listar(status, busca, pageable));
    }

    @Operation(summary = "Obter visão detalhada da obra", description = "Retorna dados cadastrais da obra, dias restantes e resumo financeiro consolidado.")
    @GetMapping("/{id}")
    public ResponseEntity<ObraDetalheResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(obraService.buscarDetalhesPorId(id));
    }

    @Operation(summary = "Obter resumo financeiro da obra", description = "Retorna totalizadores de receitas, despesas, saldo, consumo do orçamento e distribuição por centro de custo.")
    @GetMapping("/{id}/resumo-financeiro")
    public ResponseEntity<ResumoFinanceiroObraDTO> buscarResumoFinanceiro(@PathVariable Long id) {
        return ResponseEntity.ok(obraService.buscarResumoFinanceiro(id));
    }

    @Operation(summary = "Listar lançamentos financeiros de uma obra")
    @GetMapping("/{id}/lancamentos")
    public ResponseEntity<Page<LancamentoResponseDTO>> listarLancamentosDaObra(
            @PathVariable Long id,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) StatusPagamento statusPagamento,
            @ParameterObject
            @PageableDefault(size = 20, sort = "dataCompetencia", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lancamentoService.listarPorObra(id, tipo, statusPagamento, pageable));
    }

    @Operation(summary = "Cadastrar nova obra")
    @PostMapping
    public ResponseEntity<ObraResponseDTO> criar(@Valid @RequestBody ObraRequestDTO dto) {
        ObraResponseDTO criada = obraService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @Operation(summary = "Atualizar dados completos da obra")
    @PutMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ObraRequestDTO dto
    ) {
        return ResponseEntity.ok(obraService.atualizar(id, dto));
    }

    @Operation(summary = "Atualizar status e progresso da obra")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ObraResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody ObraStatusUpdateDTO dto
    ) {
        return ResponseEntity.ok(obraService.atualizarStatus(id, dto));
    }

    @Operation(summary = "Excluir obra", description = "Remove a obra e seus lançamentos financeiros vinculados em cascata.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        obraService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
