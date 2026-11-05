package br.com.prismaapi.service.recorrente;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class LancamentoRecorrenteAgendador {

    private final LancamentoRecorrenteService lancamentoRecorrenteService;

    @Scheduled(cron = "0 1 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    public void lancarVencidas() {
        lancamentoRecorrenteService.lancarVencidas();
    }
}