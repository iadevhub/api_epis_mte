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

    public CaDTO() {}

    // Status vem direto do arquivo — sem recalcular
    public CaDTO(String numeroCa, String descricao, String fabricante,
                 String cnpj, String dataValidade, String status,
                 String tipoEpi, String nrAprovacao, String restricoes) {
        this.numeroCa     = numeroCa;
        this.descricao    = descricao;
        this.fabricante   = fabricante;
        this.cnpj         = cnpj;
        this.dataValidade = dataValidade;
        this.status       = status;
        this.tipoEpi      = tipoEpi;
        this.nrAprovacao  = nrAprovacao;
        this.restricoes   = restricoes;
    }

    public String getNumeroCa()       { return numeroCa; }
    public void setNumeroCa(String v) { this.numeroCa = v; }

    public String getDescricao()       { return descricao; }
    public void setDescricao(String v) { this.descricao = v; }

    public String getFabricante()       { return fabricante; }
    public void setFabricante(String v) { this.fabricante = v; }

    public String getCnpj()       { return cnpj; }
    public void setCnpj(String v) { this.cnpj = v; }

    public String getDataValidade()       { return dataValidade; }
    public void setDataValidade(String v) { this.dataValidade = v; }

    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }

    public String getTipoEpi()       { return tipoEpi; }
    public void setTipoEpi(String v) { this.tipoEpi = v; }

    public String getNrAprovacao()       { return nrAprovacao; }
    public void setNrAprovacao(String v) { this.nrAprovacao = v; }

    public String getRestricoes()       { return restricoes; }
    public void setRestricoes(String v) { this.restricoes = v; }
}
