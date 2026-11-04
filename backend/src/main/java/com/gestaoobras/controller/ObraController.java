package com.gestaoobras.controller;

import com.gestaoobras.dto.ObraRequestDTO;
import com.gestaoobras.dto.ObraResponseDTO;
import com.gestaoobras.service.ObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/obras")
@RequiredArgsConstructor
public class ObraController {

    private final ObraService obraService;

    @GetMapping
    public ResponseEntity<List<ObraResponseDTO>> listar() {
        return ResponseEntity.ok(obraService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(obraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ObraResponseDTO> criar(@RequestBody @Valid ObraRequestDTO dto) {
        ObraResponseDTO criada = obraService.criar(dto);
        URI location = URI.create("/obras/" + criada.id());
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ObraRequestDTO dto) {
        return ResponseEntity.ok(obraService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        obraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
