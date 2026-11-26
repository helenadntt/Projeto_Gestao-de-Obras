package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.domain.enums.TipoLancamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoPagamentoUpdateDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoRequestDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoResponseDTO;
import br.com.gestaoobras.api.service.LancamentoFinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/lancamentos")
@RequiredArgsConstructor
@Tag(name = "Lançamentos Financeiros", description = "Controle de receitas, despesas, pagamentos e fluxo de caixa")
public class LancamentoFinanceiroController {

    private final LancamentoFinanceiroService lancamentoService;

    @Operation(summary = "Listar lançamentos financeiros", description = "Retorna lista paginada de lançamentos com filtros combináveis por obra, centro de custo, tipo, status e período.")
    @GetMapping
    public ResponseEntity<Page<LancamentoResponseDTO>> listar(
            @RequestParam(required = false) Long obraId,
            @RequestParam(required = false) Long centroCustoId,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) StatusPagamento statusPagamento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @ParameterObject
            @PageableDefault(size = 20, sort = "dataCompetencia", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                lancamentoService.listar(obraId, centroCustoId, tipo, statusPagamento, dataInicio, dataFim, pageable)
        );
    }

    @Operation(summary = "Buscar lançamento financeiro por ID")
    @GetMapping("/{id}")
    public ResponseEntity<LancamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(lancamentoService.buscarPorId(id));
    }

    @Operation(summary = "Registrar lançamento financeiro")
    @PostMapping
    public ResponseEntity<LancamentoResponseDTO> criar(@Valid @RequestBody LancamentoRequestDTO dto) {
        LancamentoResponseDTO criado = lancamentoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @Operation(summary = "Atualizar lançamento financeiro")
    @PutMapping("/{id}")
    public ResponseEntity<LancamentoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LancamentoRequestDTO dto
    ) {
        return ResponseEntity.ok(lancamentoService.atualizar(id, dto));
    }

    @Operation(summary = "Atualizar status de pagamento do lançamento", description = "Permite marcar lançamento como PAGO, PENDENTE ou CANCELADO.")
    @PatchMapping("/{id}/pagamento")
    public ResponseEntity<LancamentoResponseDTO> atualizarPagamento(
            @PathVariable Long id,
            @Valid @RequestBody LancamentoPagamentoUpdateDTO dto
    ) {
        return ResponseEntity.ok(lancamentoService.atualizarPagamento(id, dto));
    }

    @Operation(summary = "Excluir lançamento financeiro")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        lancamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
