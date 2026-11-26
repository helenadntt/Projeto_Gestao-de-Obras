package br.com.gestaoobras.api.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PagamentoValidoValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PagamentoValido {

    String message() default "A data de pagamento não pode ser informada quando o status de pagamento for PENDENTE.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
