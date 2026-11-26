package br.com.gestaoobras.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gestaoObrasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestão de Obras API")
                        .version("1.0.0")
                        .description("""
                                API RESTful para acompanhamento operacional e controle financeiro \
                                de projetos de construção civil (obras, centros de custo, lançamentos \
                                financeiros e indicadores de dashboard).
                                """)
                        .contact(new Contact()
                                .name("Equipe Gestão de Obras")
                                .url("https://github.com/helenadntt/Projeto_Gestao-de-Obras"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
