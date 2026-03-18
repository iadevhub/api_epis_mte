package br.com.unomed.caapi.controller;

import br.com.unomed.caapi.dto.CaDTO;
import br.com.unomed.caapi.service.CaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/ca")
@CrossOrigin(origins = "*") // Permite acesso do Gestor SST
public class CaController {

    private final CaService caService;

    @Value("${ca.reload.key}")
    private String reloadKey;

    public CaController(CaService caService) {
        this.caService = caService;
    }

    // ─────────────────────────────────────────────
    // GET /ca/12345
    // Consulta CA pelo número
    // ─────────────────────────────────────────────
    @GetMapping("/{numero}")
    public ResponseEntity<?> consultarPorNumero(@PathVariable String numero) {

        if (numero == null || numero.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Número do CA não informado"));
        }

        Optional<CaDTO> ca = caService.consultarPorNumero(numero);

        if (ca.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of(
                    "erro", "CA não encontrado",
                    "numeroCa", numero,
                    "dica", "Verifique se o número está correto. Ex: /ca/12345"
                ));
        }

        return ResponseEntity.ok(ca.get());
    }

    // ─────────────────────────────────────────────
    // GET /ca/buscar?nome=luva
    // Busca por nome/descrição do EPI
    // ─────────────────────────────────────────────
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNome(@RequestParam String nome) {

        if (nome == null || nome.length() < 3) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Informe pelo menos 3 caracteres para busca"));
        }

        List<CaDTO> resultados = caService.buscarPorNome(nome);

        if (resultados.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of(
                    "erro", "Nenhum EPI encontrado",
                    "busca", nome
                ));
        }

        return ResponseEntity.ok(Map.of(
            "total", resultados.size(),
            "resultados", resultados
        ));
    }

    // ─────────────────────────────────────────────
    // GET /ca/status
    // Informações sobre a base carregada
    // ─────────────────────────────────────────────
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(caService.getStatus());
    }

    // ─────────────────────────────────────────────
    // GET /ca/reload?key=suasenha
    // Força recarregamento da base do MTE
    // ─────────────────────────────────────────────
    @GetMapping("/reload")
    public ResponseEntity<?> reload(@RequestParam String key) {

        if (!reloadKey.equals(key)) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Chave inválida"));
        }

        String resultado = caService.carregarBase();
        return ResponseEntity.ok(Map.of(
            "mensagem", resultado,
            "totalRegistros", caService.getTotalRegistros()
        ));
    }
}
