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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class CaService {

    private static final String FTP_HOST   = "ftp.mtps.gov.br";
    private static final String FTP_DIR    =
        "portal/fiscalizacao/seguranca-e-saude-no-trabalho/caepi";
    private static final String FTP_FILE   = "tgg_export_caepi.zip";
    private static final int    FTP_PORT   = 21;
    private static final int    TIMEOUT_MS = 120_000; // 2 min — ZIP é maior

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

    // Download via FTPClient + descompacta ZIP em memória
    private List<CaDTO> baixarEParsear() throws Exception {
        List<CaDTO> lista = new ArrayList<>();

        FTPClient ftp = new FTPClient();
        ftp.setConnectTimeout(TIMEOUT_MS);
        ftp.setDefaultTimeout(TIMEOUT_MS);
        ftp.setDataTimeout(TIMEOUT_MS);
        ftp.setUseEPSVwithIPv4(true);

        try {
            ftp.connect(FTP_HOST, FTP_PORT);
            System.out.println("FTP conectado. Reply: " + ftp.getReplyCode());

            ftp.login("anonymous", "ca-api@unomed.med.br");
            System.out.println("FTP login OK. Reply: " + ftp.getReplyCode());

            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            boolean changed = ftp.changeWorkingDirectory(FTP_DIR);
            if (!changed) {
                throw new IOException("Nao foi possivel navegar para o diretorio: "
                    + FTP_DIR + " | Codigo: " + ftp.getReplyCode());
            }

            System.out.println("Baixando: " + FTP_FILE);
            InputStream ftpStream = ftp.retrieveFileStream(FTP_FILE);

            if (ftpStream == null) {
                throw new IOException("FTP recusou o arquivo. Codigo: "
                    + ftp.getReplyCode() + " " + ftp.getReplyString().trim());
            }

            // Descompacta o ZIP em memória e parseia o TXT interno
            try (ZipInputStream zip = new ZipInputStream(ftpStream)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    System.out.println("Entrada ZIP: " + entry.getName());

                    if (entry.getName().toLowerCase().endsWith(".txt")) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(zip, Charset.forName("UTF-8")));

                        String linha;
                        boolean primeiraLinha = true;

                        while ((linha = reader.readLine()) != null) {
                            if (primeiraLinha) {
                                primeiraLinha = false;
                                String[] cols = linha.split("\\|", -1);
                                System.out.println("Cabecalho (" + cols.length + " colunas): " + linha.substring(0, Math.min(linha.length(), 200)));
                                continue;
                            }
                            if (linha.isBlank()) continue;
                            CaDTO ca = parsearLinha(linha);
                            if (ca != null) lista.add(ca);
                        }

                        zip.closeEntry();
                        break; // só um TXT dentro do ZIP
                    }
                    zip.closeEntry();
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
    // Layout real do tgg_export_caepi.txt:
    // [0] NR_CA  [1] DATA_VALIDADE    [2] SITUACAO       [3] NUP
    // [4] CNPJ   [5] RAZAO_SOCIAL     [6] ABRANGENCIA    [7] TIPO_EPI
    // [8] DESCRICAO_PRODUTO           [9] MARCACAO        [10] REFERENCIAS
    private CaDTO parsearLinha(String linha) {
        try {
            String[] c = linha.split("\\|", -1);

            if (c.length < 6) return null;

            String numeroCa       = limpar(c[0]);
            String dataValidade   = limpar(c.length > 1  ? c[1]  : ""); // data de validade
            String status         = limpar(c[2]); // VÁLIDO / VENCIDO / CANCELADO
            String nup            = limpar(c.length > 3  ? c[3]  : ""); // N° Processo
            String cnpj           = limpar(c[4]);
            String fabricante     = limpar(c[5]);
            String abrangencia    = limpar(c.length > 6  ? c[6]  : ""); // Nacional / Estadual
            String tipoEpi        = limpar(c.length > 7  ? c[7]  : "");
            String descricao      = limpar(c.length > 8  ? c[8]  : "");
            String marcacao       = limpar(c.length > 9  ? c[9]  : ""); // onde o CA é impresso
            String referencias    = limpar(c.length > 10 ? c[10] : ""); // modelo/referências

            if (numeroCa.isBlank()) return null;

            return new CaDTO(numeroCa, descricao, fabricante, cnpj,
                             dataValidade, status, nup,
                             abrangencia, tipoEpi, marcacao, referencias);

        } catch (Exception e) {
            return null;
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
            .filter(ca ->
                (ca.getDescricao() != null && ca.getDescricao().toLowerCase().contains(busca))
             || (ca.getTipoEpi()   != null && ca.getTipoEpi().toLowerCase().contains(busca))
             || (ca.getFabricante()!= null && ca.getFabricante().toLowerCase().contains(busca)))
            .limit(50)
            .toList();
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status",                "online");
        status.put("totalRegistros",        totalRegistros);
        status.put("ultimaAtualizacao",     ultimaAtualizacao);
        status.put("statusUltimaCarga",     statusUltimaCarga);
        status.put("fonte",                 "ftp://" + FTP_HOST + "/" + FTP_DIR + "/" + FTP_FILE);
        status.put("atualizacaoAutomatica", "Todo dia as 20h15");
        return status;
    }

    public int getTotalRegistros() { return totalRegistros; }
}
