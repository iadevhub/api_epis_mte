package br.com.unomed.caapi.controller;

import br.com.unomed.caapi.dto.CboDTO;
import br.com.unomed.caapi.service.CboService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cbo")
@CrossOrigin(origins = "*")
public class CboController {

    private final CboService cboService;

    public CboController(CboService cboService) {
        this.cboService = cboService;
    }

    /** GET /cbo/buscar?q=motorista  — busca textual, máx 30 resultados */
    @GetMapping("/buscar")
    public ResponseEntity<List<CboDTO>> buscar(@RequestParam("q") String q) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.badRequest().build();
        }
        List<CboDTO> resultado = cboService.buscar(q);
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    /** GET /cbo/{codigo}  — detalhe por código ex: 782510 */
    @GetMapping("/{codigo}")
    public ResponseEntity<CboDTO> porCodigo(@PathVariable String codigo) {
        return cboService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /cbo/status  — total carregado */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "online",
                "totalOcupacoes", cboService.total(),
                "fonte", "MTE CBO 2002 (atualização 2020)"
        ));
    }
}
