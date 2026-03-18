package br.com.unomed.caapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaDTO {

    private String numeroCa;
    private String descricao;
    private String fabricante;
    private String cnpj;
    private String dataValidade;
    private String status;
    private String tipoEpi;
    private String nrAprovacao;
    private String restricoes;

    // Construtor vazio
    public CaDTO() {}

    // Construtor completo
    public CaDTO(String numeroCa, String descricao, String fabricante,
                 String cnpj, String dataValidade, String tipoEpi,
                 String nrAprovacao, String restricoes) {
        this.numeroCa     = numeroCa;
        this.descricao    = descricao;
        this.fabricante   = fabricante;
        this.cnpj         = cnpj;
        this.dataValidade = dataValidade;
        this.tipoEpi      = tipoEpi;
        this.nrAprovacao  = nrAprovacao;
        this.restricoes   = restricoes;
        this.status       = calcularStatus(dataValidade);
    }

    private String calcularStatus(String dataValidade) {
        if (dataValidade == null || dataValidade.isBlank()) return "SEM DATA";
        try {
            // Formato do gov: dd/MM/yyyy
            String[] partes = dataValidade.split("/");
            if (partes.length != 3) return "SEM DATA";
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);

            java.time.LocalDate validade = java.time.LocalDate.of(ano, mes, dia);
            return validade.isBefore(java.time.LocalDate.now()) ? "VENCIDO" : "VÁLIDO";
        } catch (Exception e) {
            return "SEM DATA";
        }
    }

    // Getters e Setters
    public String getNumeroCa()      { return numeroCa; }
    public void setNumeroCa(String v){ this.numeroCa = v; }

    public String getDescricao()      { return descricao; }
    public void setDescricao(String v){ this.descricao = v; }

    public String getFabricante()      { return fabricante; }
    public void setFabricante(String v){ this.fabricante = v; }

    public String getCnpj()      { return cnpj; }
    public void setCnpj(String v){ this.cnpj = v; }

    public String getDataValidade()      { return dataValidade; }
    public void setDataValidade(String v){ this.dataValidade = v; }

    public String getStatus()      { return status; }
    public void setStatus(String v){ this.status = v; }

    public String getTipoEpi()      { return tipoEpi; }
    public void setTipoEpi(String v){ this.tipoEpi = v; }

    public String getNrAprovacao()      { return nrAprovacao; }
    public void setNrAprovacao(String v){ this.nrAprovacao = v; }

    public String getRestricoes()      { return restricoes; }
    public void setRestricoes(String v){ this.restricoes = v; }
}
