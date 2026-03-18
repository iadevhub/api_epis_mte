package br.com.unomed.caapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaDTO {

    // Colunas do tgg_export_caepi.txt (separador |, 19 colunas)
    private String numeroCa;              // c[0]  #NRRegistroCA
    private String dataValidade;          // c[1]  DataValidade
    private String status;               // c[2]  Situacao
    private String nup;                  // c[3]  NRProcesso
    private String cnpj;                 // c[4]  CNPJ
    private String fabricante;           // c[5]  RazaoSocial
    private String abrangencia;          // c[6]  Natureza
    private String tipoEpi;             // c[7]  NomeEquipamento
    private String descricao;           // c[8]  DescricaoEquipamento
    private String marcacao;            // c[9]  MarcaCA
    private String referencias;         // c[10] Referencia
    private String cor;                 // c[11] Cor
    private String aprovadoPara;        // c[12] AprovadoParaLaudo
    private String restricaoLaudo;      // c[13] RestricaoLaudo
    private String observacao;          // c[14] ObservacaoAnaliseLaudo
    private String cnpjLaboratorio;     // c[15] CNPJLaboratorio
    private String laboratorio;         // c[16] RazaoSocialLaboratorio
    private String nrLaudo;             // c[17] NRLaudo
    private String norma;               // c[18] Norma

    public CaDTO() {}

    public CaDTO(String numeroCa, String dataValidade, String status, String nup,
                 String cnpj, String fabricante, String abrangencia, String tipoEpi,
                 String descricao, String marcacao, String referencias, String cor,
                 String aprovadoPara, String restricaoLaudo, String observacao,
                 String cnpjLaboratorio, String laboratorio, String nrLaudo, String norma) {
        this.numeroCa       = numeroCa;
        this.dataValidade   = dataValidade;
        this.status         = status;
        this.nup            = nup;
        this.cnpj           = cnpj;
        this.fabricante     = fabricante;
        this.abrangencia    = abrangencia;
        this.tipoEpi        = tipoEpi;
        this.descricao      = descricao;
        this.marcacao       = marcacao;
        this.referencias    = referencias;
        this.cor            = cor;
        this.aprovadoPara   = aprovadoPara;
        this.restricaoLaudo = restricaoLaudo;
        this.observacao     = observacao;
        this.cnpjLaboratorio = cnpjLaboratorio;
        this.laboratorio    = laboratorio;
        this.nrLaudo        = nrLaudo;
        this.norma          = norma;
    }

    public String getNumeroCa()              { return numeroCa; }
    public void setNumeroCa(String v)        { numeroCa = v; }

    public String getDataValidade()          { return dataValidade; }
    public void setDataValidade(String v)    { dataValidade = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { status = v; }

    public String getNup()                   { return nup; }
    public void setNup(String v)             { nup = v; }

    public String getCnpj()                  { return cnpj; }
    public void setCnpj(String v)            { cnpj = v; }

    public String getFabricante()            { return fabricante; }
    public void setFabricante(String v)      { fabricante = v; }

    public String getAbrangencia()           { return abrangencia; }
    public void setAbrangencia(String v)     { abrangencia = v; }

    public String getTipoEpi()               { return tipoEpi; }
    public void setTipoEpi(String v)         { tipoEpi = v; }

    public String getDescricao()             { return descricao; }
    public void setDescricao(String v)       { descricao = v; }

    public String getMarcacao()              { return marcacao; }
    public void setMarcacao(String v)        { marcacao = v; }

    public String getReferencias()           { return referencias; }
    public void setReferencias(String v)     { referencias = v; }

    public String getCor()                   { return cor; }
    public void setCor(String v)             { cor = v; }

    public String getAprovadoPara()          { return aprovadoPara; }
    public void setAprovadoPara(String v)    { aprovadoPara = v; }

    public String getRestricaoLaudo()        { return restricaoLaudo; }
    public void setRestricaoLaudo(String v)  { restricaoLaudo = v; }

    public String getObservacao()            { return observacao; }
    public void setObservacao(String v)      { observacao = v; }

    public String getCnpjLaboratorio()         { return cnpjLaboratorio; }
    public void setCnpjLaboratorio(String v)   { cnpjLaboratorio = v; }

    public String getLaboratorio()           { return laboratorio; }
    public void setLaboratorio(String v)     { laboratorio = v; }

    public String getNrLaudo()               { return nrLaudo; }
    public void setNrLaudo(String v)         { nrLaudo = v; }

    public String getNorma()                 { return norma; }
    public void setNorma(String v)           { norma = v; }
}
