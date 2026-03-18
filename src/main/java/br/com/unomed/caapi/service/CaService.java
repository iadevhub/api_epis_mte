package br.com.unomed.caapi.service;

import br.com.unomed.caapi.dto.CaDTO;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaService {

    private static final String FTP_HOST   = "ftp.mtps.gov.br";
    private static final String FTP_PATH   =
        "/portal/fiscalizacao/seguranca-e-saude-no-trabalho/caepi/tgg_export_caepi.txt";
    private static final int    FTP_PORT   = 21;
    private static final int    TIMEOUT_MS = 60_000; // 60 segundos

    // HashMap em memória — zero banco de dados
    private final Map<String, CaDTO> cache = new ConcurrentHashMap<>();

    // Metadados
    private String ultimaAtualizacao  = "Nunca";
    private int    totalRegistros     = 0;
    private String statusUltimaCarga  = "Aguardando primeira carga";

    // Carrega ao subir a aplicação
    public CaService() {
        System.out.println("CaService iniciado. Carregando base CA do MTE...");
        carregarBase();
    }

    // Atualização automática todo dia às 20h15
    @Scheduled(cron = "0 15 20 * * *")
    public void atualizacaoAutomatica() {
        System.out.println("Atualizacao automatica agendada iniciando...");
        carregarBase();
    }

    // Método principal de carga
    public synchronized String carregarBase() {
        try {
            System.out.println("Conectando ao FTP do MTE...");
            List<CaDTO> lista = baixarEParsear();

            if (lista.isEmpty()) {
                statusUltimaCarga = "ERRO: Arquivo vazio ou nao encontrado";
                System.out.println(statusUltimaCarga);
                return statusUltimaCarga;
            }

            cache.clear();
            for (CaDTO ca : lista) {
                if (ca.getNumeroCa() != null && !ca.getNumeroCa().isBlank()) {
                    cache.put(ca.getNumeroCa().trim(), ca);
                }
            }

            totalRegistros    = cache.size();
            ultimaAtualizacao = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            statusUltimaCarga = "OK";

            System.out.println("Base carregada: " + totalRegistros + " CAs em memoria.");
            return "Base recarregada com sucesso. " + totalRegistros
                + " registros. Data: " + ultimaAtualizacao;

        } catch (Exception e) {
            statusUltimaCarga = "ERRO: " + e.getMessage();
            System.out.println("Erro ao carregar base: " + e.getMessage());
            return "Erro ao carregar base: " + e.getMessage();
        }
    }

    // Download via FTPClient (commons-net) — funciona em containers
    private List<CaDTO> baixarEParsear() throws Exception {
        List<CaDTO> lista = new ArrayList<>();

        FTPClient ftp = new FTPClient();
        ftp.setConnectTimeout(TIMEOUT_MS);
        ftp.setDefaultTimeout(TIMEOUT_MS);
        ftp.setDataTimeout(TIMEOUT_MS);

        try {
            ftp.connect(FTP_HOST, FTP_PORT);
            ftp.login("anonymous", "ca-api@unomed.med.br");
            ftp.setFileType(FTP.ASCII_FILE_TYPE);
            ftp.enterLocalPassiveMode(); // PASV — essencial em containers

            try (InputStream is = ftp.retrieveFileStream(FTP_PATH);
                 BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, Charset.forName("ISO-8859-1")))) {

                String linha;
                boolean primeiraLinha = true;

                while ((linha = reader.readLine()) != null) {

                    if (primeiraLinha) {
                        primeiraLinha = false;
                        System.out.println("Cabecalho: "
                            + linha.substring(0, Math.min(linha.length(), 120)));
                        continue;
                    }

                    if (linha.isBlank()) continue;

                    CaDTO ca = parsearLinha(linha);
                    if (ca != null) lista.add(ca);
                }
            }

            ftp.completePendingCommand();

        } finally {
            if (ftp.isConnected()) {
                try { ftp.logout(); } catch (Exception ignored) {}
                try { ftp.disconnect(); } catch (Exception ignored) {}
            }
        }

        return lista;
    }

    // Parse de cada linha do arquivo TXT (separado por |)
    private CaDTO parsearLinha(String linha) {
        try {
            String[] c = linha.split("\\|", -1);

            if (c.length < 5) return null;

            // Posições do tgg_export_caepi.txt
            // Ajuste se o gov alterar o layout (ver logs "Cabecalho:")
            String numeroCa     = limpar(c.length > 0 ? c[0] : "");
            String descricao    = limpar(c.length > 1 ? c[1] : "");
            String tipoEpi      = limpar(c.length > 2 ? c[2] : "");
            String fabricante   = limpar(c.length > 3 ? c[3] : "");
            String cnpj         = limpar(c.length > 4 ? c[4] : "");
            String dataValidade = limpar(c.length > 5 ? c[5] : "");
            String nrAprovacao  = limpar(c.length > 6 ? c[6] : "");
            String restricoes   = limpar(c.length > 7 ? c[7] : "");

            if (numeroCa.isBlank()) return null;

            return new CaDTO(numeroCa, descricao, fabricante, cnpj,
                             dataValidade, tipoEpi, nrAprovacao, restricoes);

        } catch (Exception e) {
            return null; // linha malformada — ignora
        }
    }

    private String limpar(String s) {
        if (s == null) return "";
        return s.trim().replace("\"", "").replace("\r", "");
    }

    public Optional<CaDTO> consultarPorNumero(String numeroCa) {
        return Optional.ofNullable(cache.get(numeroCa.trim()));
    }

    public List<CaDTO> buscarPorNome(String nome) {
        String busca = nome.toLowerCase().trim();
        return cache.values().stream()
            .filter(ca -> ca.getDescricao() != null
                       && ca.getDescricao().toLowerCase().contains(busca))
            .limit(50)
            .toList();
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status",              "online");
        status.put("totalRegistros",      totalRegistros);
        status.put("ultimaAtualizacao",   ultimaAtualizacao);
        status.put("statusUltimaCarga",   statusUltimaCarga);
        status.put("fonte",               FTP_HOST + FTP_PATH);
        status.put("atualizacaoAutomatica", "Todo dia as 20h15");
        return status;
    }

    public int getTotalRegistros() { return totalRegistros; }
}
