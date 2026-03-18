package br.com.unomed.caapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaDTO {

    private String numeroCa;
    private String descricao;
    private String fabricante;
    private String cnpj;
    private String dataValidade;   // c[1] — data de validade do CA
    private String status;         // c[2] — VÁLIDO / VENCIDO / CANCELADO
    private String nup;            // c[3] — N° Único de Processo
    private String abrangencia;    // c[6] — Nacional / Estadual
    private String tipoEpi;        // c[7] — tipo do EPI
    private String marcacao;       // c[9] — onde o CA está impresso no produto
    private String referencias;    // c[10] — referências / modelo

    public CaDTO() {}

    public CaDTO(String numeroCa, String descricao, String fabricante, String cnpj,
                 String dataValidade, String status, String nup,
                 String abrangencia, String tipoEpi,
                 String marcacao, String referencias) {
        this.numeroCa      = numeroCa;
        this.descricao     = descricao;
        this.fabricante    = fabricante;
        this.cnpj          = cnpj;
        this.dataValidade = dataValidade;
        this.status        = status;
        this.nup           = nup;
        this.abrangencia   = abrangencia;
        this.tipoEpi       = tipoEpi;
        this.marcacao      = marcacao;
        this.referencias   = referencias;
    }

    public String getNumeroCa()             { return numeroCa; }
    public void setNumeroCa(String v)       { this.numeroCa = v; }

    public String getDescricao()            { return descricao; }
    public void setDescricao(String v)      { this.descricao = v; }

    public String getFabricante()           { return fabricante; }
    public void setFabricante(String v)     { this.fabricante = v; }

    public String getCnpj()                 { return cnpj; }
    public void setCnpj(String v)           { this.cnpj = v; }

    public String getDataValidade()         { return dataValidade; }
    public void setDataValidade(String v)   { this.dataValidade = v; }

    public String getStatus()               { return status; }
    public void setStatus(String v)         { this.status = v; }

    public String getNup()                  { return nup; }
    public void setNup(String v)            { this.nup = v; }

    public String getAbrangencia()          { return abrangencia; }
    public void setAbrangencia(String v)    { this.abrangencia = v; }

    public String getTipoEpi()              { return tipoEpi; }
    public void setTipoEpi(String v)        { this.tipoEpi = v; }

    public String getMarcacao()             { return marcacao; }
    public void setMarcacao(String v)       { this.marcacao = v; }

    public String getReferencias()          { return referencias; }
    public void setReferencias(String v)    { this.referencias = v; }
}
