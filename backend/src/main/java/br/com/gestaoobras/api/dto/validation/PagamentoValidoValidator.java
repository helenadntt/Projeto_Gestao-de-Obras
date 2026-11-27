package br.com.gestaoobras.api.dto.validation;

import br.com.gestaoobras.api.domain.enums.StatusPagamento;
import br.com.gestaoobras.api.dto.lancamento.LancamentoPagamentoUpdateDTO;
import br.com.gestaoobras.api.dto.lancamento.LancamentoRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class PagamentoValidoValidator implements ConstraintValidator<PagamentoValido, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        StatusPagamento status = null;
        LocalDate dataPagamento = null;

        if (value instanceof LancamentoRequestDTO dto) {
            status = dto.statusPagamento();
            dataPagamento = dto.dataPagamento();
        } else if (value instanceof LancamentoPagamentoUpdateDTO dto) {
            status = dto.statusPagamento();
            dataPagamento = dto.dataPagamento();
        } else {
            return true;
        }

        boolean statusPendente = status == null || status == StatusPagamento.PENDENTE;
        if (statusPendente && dataPagamento != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "A data de pagamento não pode ser informada quando o status de pagamento for PENDENTE.")
                    .addPropertyNode("dataPagamento")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
