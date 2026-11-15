package br.com.agenday.config;

import br.com.agenday.model.Servico;
import br.com.agenday.repository.ServicoRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarregadorDadosIniciais {

    @Bean
    public CommandLineRunner carregarServicosIniciais(ServicoRepository servicoRepository) {
        return args -> {
            if (servicoRepository.count() == 0) {
                servicoRepository.save(new Servico(
                        "Massagem Relaxante",
                        "Alívio de tensões musculares, redução do estresse e relaxamento profundo.",
                        60,
                        new BigDecimal("120.00")
                ));

                servicoRepository.save(new Servico(
                        "Massagem Terapêutica",
                        "Técnicas focadas no tratamento de dores crônicas, nós musculares e reabilitação.",
                        90,
                        new BigDecimal("160.00")
                ));

                servicoRepository.save(new Servico(
                        "Drenagem Linfática",
                        "Estimula a circulação, reduz retenção de líquidos e elimina toxinas corporais.",
                        60,
                        new BigDecimal("130.00")
                ));

                System.out.println(">>> [Agenday] 3 serviços iniciais foram cadastrados no banco com sucesso!");
            }
        };
    }
}
