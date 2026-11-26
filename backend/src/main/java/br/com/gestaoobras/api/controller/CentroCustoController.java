package br.com.gestaoobras.api.controller;

import br.com.gestaoobras.api.dto.centrocusto.CentroCustoRequestDTO;
import br.com.gestaoobras.api.dto.centrocusto.CentroCustoResponseDTO;
import br.com.gestaoobras.api.service.CentroCustoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/centros-custo")
@RequiredArgsConstructor
@Tag(name = "Centros de Custo", description = "Classificação e categorização dos lançamentos financeiros")
public class CentroCustoController {

    private final CentroCustoService centroCustoService;

    @Operation(summary = "Listar centros de custo")
    @GetMapping
    public ResponseEntity<List<CentroCustoResponseDTO>> listar(
            @Parameter(description = "Quando true, retorna apenas centros de custo ativos")
            @RequestParam(defaultValue = "false") boolean apenasAtivos
    ) {
        return ResponseEntity.ok(centroCustoService.listar(apenasAtivos));
    }

    @Operation(summary = "Buscar centro de custo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CentroCustoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(centroCustoService.buscarPorId(id));
    }

    @Operation(summary = "Cadastrar centro de custo")
    @PostMapping
    public ResponseEntity<CentroCustoResponseDTO> criar(@Valid @RequestBody CentroCustoRequestDTO dto) {
        CentroCustoResponseDTO criado = centroCustoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @Operation(summary = "Atualizar centro de custo")
    @PutMapping("/{id}")
    public ResponseEntity<CentroCustoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CentroCustoRequestDTO dto
    ) {
        return ResponseEntity.ok(centroCustoService.atualizar(id, dto));
    }

    @Operation(summary = "Excluir centro de custo", description = "Exclui o centro de custo preservando os lançamentos financeiros (vínculo passa a ser nulo).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        centroCustoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
