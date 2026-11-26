package br.com.gestaoobras.api.domain.entity;

import br.com.gestaoobras.api.domain.enums.StatusObra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "obras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "cliente", length = 150)
    private String cliente;

    @Column(name = "endereco", nullable = false, length = 255)
    private String endereco;

    @Column(name = "responsavel", nullable = false, length = 100)
    private String responsavel;

    @Column(name = "orcamento_previsto", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal orcamentoPrevisto = BigDecimal.ZERO;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "previsao_fim", nullable = false)
    private LocalDate previsaoFim;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private StatusObra status = StatusObra.PLANEJAMENTO;

    @Column(name = "progresso_percentual", nullable = false)
    @Builder.Default
    private Integer progressoPercentual = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.status == null) {
            this.status = StatusObra.PLANEJAMENTO;
        }
        if (this.orcamentoPrevisto == null) {
            this.orcamentoPrevisto = BigDecimal.ZERO;
        }
        if (this.progressoPercentual == null) {
            this.progressoPercentual = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
