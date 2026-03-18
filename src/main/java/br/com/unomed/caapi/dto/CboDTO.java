package br.com.unomed.caapi.dto;

import java.util.List;

public class CboDTO {

    private String codigo;
    private String ocupacao;
    private String familia;
    private String subGrupo;
    private String subGrupoPrincipal;
    private String grandeGrupo;
    private List<String> sinonimos;

    public CboDTO() {}

    public CboDTO(String codigo, String ocupacao, String familia,
                  String subGrupo, String subGrupoPrincipal, String grandeGrupo,
                  List<String> sinonimos) {
        this.codigo            = codigo;
        this.ocupacao          = ocupacao;
        this.familia           = familia;
        this.subGrupo          = subGrupo;
        this.subGrupoPrincipal = subGrupoPrincipal;
        this.grandeGrupo       = grandeGrupo;
        this.sinonimos         = sinonimos;
    }

    public String getCodigo()            { return codigo; }
    public String getOcupacao()          { return ocupacao; }
    public String getFamilia()           { return familia; }
    public String getSubGrupo()          { return subGrupo; }
    public String getSubGrupoPrincipal() { return subGrupoPrincipal; }
    public String getGrandeGrupo()       { return grandeGrupo; }
    public List<String> getSinonimos()   { return sinonimos; }
}
