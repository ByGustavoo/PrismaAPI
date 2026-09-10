package br.com.prismaapi.exceptions.handler;

import br.com.prismaapi.exceptions.*;
import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.exceptions.dto.MethodArgumentNotValidResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest pHttpServletRequest) {

        var errors = ex.getFieldErrors()
                .stream()
                .map(fieldError -> new MethodArgumentNotValidResponseDTO(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();

        var response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/validation-error",
                "A requisição contém dados inválidos!",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição Inválida!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/unreadable-message",
                ex.getMostSpecificCause().getMessage(),
                ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetros Inválidos!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/invalid-parameters",
                "O parâmetro '" + ex.getName() + "' foi informado num formato inválido!",
                ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> handleRequisicaoInvalidaException(RequisicaoInvalidaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição Inválida!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/invalid-request",
                ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição Inválida!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/illegal-argument",
                "A requisição contém dados inválidos!",
                ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(LancamentoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleLancamentoNaoEncontradoException(LancamentoNaoEncontradoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Lançamento não encontrado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/lancamento-nao-encontrado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaNaoEncontradaException(ContaNaoEncontradaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Conta não encontrada!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-nao-encontrada",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CartaoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleCartaoNaoEncontradoException(CartaoNaoEncontradoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Cartão não encontrado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/cartao-nao-encontrado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(FaturaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleFaturaNaoEncontradaException(FaturaNaoEncontradaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Fatura não encontrada!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/fatura-nao-encontrada",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CompraParceladaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleCompraParceladaNaoEncontradaException(CompraParceladaNaoEncontradaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Compra parcelada não encontrada!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/compra-parcelada-nao-encontrada",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvestimentoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvestimentoNaoEncontradoException(InvestimentoNaoEncontradoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Investimento não encontrado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/investimento-nao-encontrado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(OrcamentoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrcamentoNaoEncontradoException(OrcamentoNaoEncontradoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Orçamento não encontrado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/orcamento-nao-encontrado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DespesaRecorrenteNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleDespesaRecorrenteNaoEncontradaException(DespesaRecorrenteNaoEncontradaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Despesa recorrente não encontrada!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/despesa-recorrente-nao-encontrada",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MetaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleMetaNaoEncontradaException(MetaNaoEncontradaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Meta não encontrada!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/meta-nao-encontrada",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Registro não encontrado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/entity-not-found",
                "Não foi possível localizar um registro com o ID informado!",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/resource-not-found",
                "O endpoint informado não existe!",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método Não Permitido!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/method-not-allowed",
                "O método '" + ex.getMethod() + "' não é aceito neste endpoint!",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(ContaDuplicadaException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaDuplicadaException(ContaDuplicadaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conta Duplicada!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-duplicada",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ContaComHistoricoException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaComHistoricoException(ContaComHistoricoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conta com Histórico!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-com-historico",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ContaComCartaoVinculadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaComCartaoVinculadoException(ContaComCartaoVinculadoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conta com Cartão Vinculado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-com-cartao-vinculado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CartaoComHistoricoException.class)
    public ResponseEntity<ErrorResponseDTO> handleCartaoComHistoricoException(CartaoComHistoricoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Cartão com Histórico!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/cartao-com-historico",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OrcamentoDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrcamentoDuplicadoException(OrcamentoDuplicadoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Orçamento Duplicado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/orcamento-duplicado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PrecoDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handlePrecoDuplicadoException(PrecoDuplicadoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Preço Duplicado!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/preco-duplicado",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(DataIntegrityViolationException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflito de Dados!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/data-integrity-violation",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OrigemInexistenteException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrigemInexistenteException(OrigemInexistenteException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Origem Inexistente!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/origem-inexistente",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(ContaDestinoInexistenteException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaDestinoInexistenteException(ContaDestinoInexistenteException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Conta de Destino Inexistente!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-destino-inexistente",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(OrigemTransferenciaInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrigemTransferenciaInvalidaException(OrigemTransferenciaInvalidaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Origem da Transferência Inválida!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/origem-transferencia-invalida",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(ContaDestinoIgualOrigemException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaDestinoIgualOrigemException(ContaDestinoIgualOrigemException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Conta de Destino Igual à Origem!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-destino-igual-origem",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(CategoriaInexistenteException.class)
    public ResponseEntity<ErrorResponseDTO> handleCategoriaInexistenteException(CategoriaInexistenteException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Categoria Inexistente!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/categoria-inexistente",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(ContaVinculadaInexistenteException.class)
    public ResponseEntity<ErrorResponseDTO> handleContaVinculadaInexistenteException(ContaVinculadaInexistenteException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Conta Vinculada Inexistente!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/conta-vinculada-inexistente",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(CartaoInexistenteException.class)
    public ResponseEntity<ErrorResponseDTO> handleCartaoInexistenteException(CartaoInexistenteException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Cartão Inexistente!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/cartao-inexistente",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(CartaoNaoAceitaParcelamentoException.class)
    public ResponseEntity<ErrorResponseDTO> handleCartaoNaoAceitaParcelamentoException(CartaoNaoAceitaParcelamentoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Cartão Não Aceita Parcelamento!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/cartao-nao-aceita-parcelamento",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(CategoriaDeReceitaException.class)
    public ResponseEntity<ErrorResponseDTO> handleCategoriaDeReceitaException(CategoriaDeReceitaException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Categoria de Receita!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/categoria-de-receita",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(DataAnteriorAoPrimeiroPrecoException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAnteriorAoPrimeiroPrecoException(DataAnteriorAoPrimeiroPrecoException ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Data Anterior ao Primeiro Preço!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/data-anterior-ao-primeiro-preco",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleInternalServerErrorException(Exception ex, HttpServletRequest pHttpServletRequest) {

        var response = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno no Servidor!",
                pHttpServletRequest.getRequestURI(),
                "/PrismaAPI/problems/internal-server-error",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}