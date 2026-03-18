package br.com.unomed.caapi.service;

import br.com.unomed.caapi.dto.CboDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Carrega profissoes.sql em memória na inicialização.
 * Estrutura do INSERT:
 *   (id,'cbo','grandeGrupo','subPrincipal','subGrupo','familia','ocupacao','nome',ts,ts)
 * onde 'nome' pode ser a ocupação principal ou um sinônimo.
 */
@Service
public class CboService {

    private static final Logger log = LoggerFactory.getLogger(CboService.class);

    // codigo -> CboDTO (agrupado)
    private final Map<String, CboDTO> porCodigo = new LinkedHashMap<>();

    // lista flat para busca textual: [codigo, nome, familia, ocupacao]
    private record Entrada(String codigo, String nome, String familia, String ocupacao,
                           String subGrupo, String subGrupoPrincipal, String grandeGrupo) {}
    private final List<Entrada> entradas = new ArrayList<>();

    @PostConstruct
    public void carregar() {
        try {
            log.info("Carregando CBO de profissoes.sql...");

            // Agrupa: codigo -> lista de nomes
            Map<String, List<String[]>> agrupado = new LinkedHashMap<>();

            ClassPathResource resource = new ClassPathResource("profissoes.sql");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String linha;
                while ((linha = reader.readLine()) != null) {
                    linha = linha.trim();
                    // Linha de INSERT de dados começa com '('
                    if (!linha.startsWith("(")) continue;

                    String[] campos = parseLinha(linha);
                    if (campos == null || campos.length < 8) continue;

                    // campos: [0]=id [1]=cbo [2]=grande [3]=subPrinc [4]=sub [5]=familia [6]=ocupacao [7]=nome
                    String cbo        = limpar(campos[1]);
                    String grande     = limpar(campos[2]);
                    String subPrinc   = limpar(campos[3]);
                    String sub        = limpar(campos[4]);
                    String familia    = limpar(campos[5]);
                    String ocupacao   = limpar(campos[6]);
                    String nome       = limpar(campos[7]);

                    agrupado.computeIfAbsent(cbo, k -> new ArrayList<>())
                            .add(new String[]{grande, subPrinc, sub, familia, ocupacao, nome});
                }
            }

            // Monta CboDTO agrupando sinônimos
            for (Map.Entry<String, List<String[]>> entry : agrupado.entrySet()) {
                String cbo = entry.getKey();
                List<String[]> rows = entry.getValue();

                String[] primeira   = rows.get(0);
                String grandeGrupo  = primeira[0];
                String subPrincipal = primeira[1];
                String subGrupo     = primeira[2];
                String familia      = primeira[3];
                String ocupacao     = primeira[4]; // nivel5 (nome oficial)

                List<String> sinonimos = rows.stream()
                        .map(r -> r[5])
                        .filter(n -> !n.equalsIgnoreCase(ocupacao))
                        .distinct()
                        .collect(Collectors.toList());

                CboDTO dto = new CboDTO(cbo, ocupacao, familia, subGrupo,
                                        subPrincipal, grandeGrupo, sinonimos);
                porCodigo.put(cbo, dto);

                // Indexa ocupação principal
                entradas.add(new Entrada(cbo, ocupacao, familia, ocupacao,
                                         subGrupo, subPrincipal, grandeGrupo));
                // Indexa cada sinônimo separadamente para busca
                for (String sin : sinonimos) {
                    entradas.add(new Entrada(cbo, sin, familia, ocupacao,
                                             subGrupo, subPrincipal, grandeGrupo));
                }
            }

            log.info("CBO carregado: {} ocupações, {} entradas indexadas",
                     porCodigo.size(), entradas.size());

        } catch (Exception e) {
            log.error("Erro ao carregar CBO: {}", e.getMessage(), e);
        }
    }

    public Optional<CboDTO> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(porCodigo.get(codigo.trim()));
    }

    /**
     * Busca textual: retorna ocupações cujo nome, sinônimo ou família contém o termo.
     * Retorna no máximo 30 resultados, sem duplicatas de código.
     */
    public List<CboDTO> buscar(String termo) {
        String t = normalizar(termo.trim());
        Set<String> vistos = new LinkedHashSet<>();
        List<CboDTO> resultado = new ArrayList<>();

        for (Entrada e : entradas) {
            if (vistos.contains(e.codigo())) continue;
            if (normalizar(e.nome()).contains(t)
                    || normalizar(e.familia()).contains(t)
                    || normalizar(e.ocupacao()).contains(t)) {
                vistos.add(e.codigo());
                resultado.add(porCodigo.get(e.codigo()));
                if (resultado.size() >= 30) break;
            }
        }
        return resultado;
    }

    /** Remove acentos e converte para minúsculas — permite busca sem acento */
    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    public int total() { return porCodigo.size(); }

    // -------------------------------------------------------------------------
    // Parse de linha SQL:  (1,'782510','Grande...','Sub...', ..., 'nome','ts','ts'),
    // -------------------------------------------------------------------------
    private String[] parseLinha(String linha) {
        // Remove trailing '),' ou ')' e o '(' inicial
        if (linha.endsWith("),")) linha = linha.substring(0, linha.length() - 2);
        else if (linha.endsWith(")"))  linha = linha.substring(0, linha.length() - 1);
        if (linha.startsWith("("))    linha = linha.substring(1);

        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean dentroAspa = false;
        int i = 0;

        while (i < linha.length()) {
            char c = linha.charAt(i);
            if (c == '\'' && !dentroAspa) {
                dentroAspa = true;
                i++;
            } else if (c == '\'' && dentroAspa) {
                // Aspas escapada: ''
                if (i + 1 < linha.length() && linha.charAt(i + 1) == '\'') {
                    sb.append('\'');
                    i += 2;
                } else {
                    dentroAspa = false;
                    i++;
                }
            } else if (c == ',' && !dentroAspa) {
                campos.add(sb.toString());
                sb.setLength(0);
                i++;
            } else {
                sb.append(c);
                i++;
            }
        }
        campos.add(sb.toString());

        return campos.toArray(new String[0]);
    }

    private String limpar(String s) {
        if (s == null) return "";
        String v = s.trim();
        return v.equalsIgnoreCase("NULL") ? "" : v;
    }
}
