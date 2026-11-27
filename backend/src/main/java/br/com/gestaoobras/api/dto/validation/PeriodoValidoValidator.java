package br.com.gestaoobras.api.dto.validation;

import br.com.gestaoobras.api.dto.obra.ObraRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PeriodoValidoValidator implements ConstraintValidator<PeriodoValido, ObraRequestDTO> {

    @Override
    public boolean isValid(ObraRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.dataInicio() == null || dto.previsaoFim() == null) {
            return true;
        }

        boolean valido = true;

        if (dto.previsaoFim().isBefore(dto.dataInicio())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "A data de previsão de término não pode ser anterior à data de início.")
                    .addPropertyNode("previsaoFim")
                    .addConstraintViolation();
            valido = false;
        }

        if (dto.dataConclusao() != null && dto.dataConclusao().isBefore(dto.dataInicio())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "A data de conclusão não pode ser anterior à data de início.")
                    .addPropertyNode("dataConclusao")
                    .addConstraintViolation();
            valido = false;
        }

        return valido;
    }
}
