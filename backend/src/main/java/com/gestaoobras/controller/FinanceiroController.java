package com.gestaoobras.controller;

import com.gestaoobras.dto.FinanceiroRequestDTO;
import com.gestaoobras.dto.FinanceiroResponseDTO;
import com.gestaoobras.service.FinanceiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/obras/{obraId}/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    @GetMapping
    public ResponseEntity<List<FinanceiroResponseDTO>> listar(@PathVariable Long obraId) {
        return ResponseEntity.ok(financeiroService.listarPorObra(obraId));
    }

    @PostMapping
    public ResponseEntity<FinanceiroResponseDTO> criar(
            @PathVariable Long obraId,
            @RequestBody @Valid FinanceiroRequestDTO dto) {
        FinanceiroResponseDTO criado = financeiroService.criar(obraId, dto);
        URI location = URI.create("/obras/" + obraId + "/financeiro/" + criado.id());
        return ResponseEntity.created(location).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long obraId, @PathVariable Long id) {
        financeiroService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
