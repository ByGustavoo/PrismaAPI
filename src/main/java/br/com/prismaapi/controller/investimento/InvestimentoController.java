package br.com.prismaapi.controller.investimento;

import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.ExtratoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.service.investimento.InvestimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/investimentos")
public class InvestimentoController implements InvestimentoDocs {

    private final InvestimentoService investimentoService;

    @Override
    public ResponseEntity<CarteiraDTO> buscarCarteira() {
        return ResponseEntity.ok(investimentoService.resumirCarteira());
    }

    @Override
    public ResponseEntity<ExtratoInvestimentoDTO> buscarExtrato(UUID id) {
        return ResponseEntity.ok(investimentoService.buscarExtrato(id));
    }

    @Override
    public ResponseEntity<InvestimentoDTO> salvarInvestimento(SalvarInvestimentoDTO salvarInvestimentoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investimentoService.salvar(salvarInvestimentoDTO));
    }

    @Override
    public ResponseEntity<InvestimentoDTO> atualizarInvestimento(UUID id, AtualizarInvestimentoDTO atualizarInvestimentoDTO) {
        return ResponseEntity.ok(investimentoService.atualizar(id, atualizarInvestimentoDTO));
    }

    @Override
    public ResponseEntity<InvestimentoDTO> registrarAporte(UUID id, SalvarAporteInvestimentoDTO salvarAporteInvestimentoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investimentoService.registrarAporte(id, salvarAporteInvestimentoDTO));
    }

    @Override
    public ResponseEntity<InvestimentoDTO> registrarSaldo(UUID id, SalvarSaldoInvestimentoDTO salvarSaldoInvestimentoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investimentoService.registrarSaldo(id, salvarSaldoInvestimentoDTO));
    }

    @Override
    public ResponseEntity<Void> deletarInvestimento(UUID id) {
        investimentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}