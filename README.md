# CA-API Unomed 🟢
## API de Consulta de Certificado de Aprovação (CA) de EPI

**Responsável:** Arildo Stepenovski - MTE 27545  
**Empresa:** Stepenovski Clínica Médica Ltda - UNOMED

---

## 📦 O QUE É

API Spring Boot sem banco de dados que:
1. Baixa o arquivo oficial do MTE via FTP na inicialização
2. Carrega todos os CAs em memória (HashMap)
3. Serve consultas instantâneas por número de CA
4. Atualiza automaticamente todo dia às 20h15

**Fonte dos dados:**  
`ftp://ftp.mtps.gov.br/portal/fiscalizacao/seguranca-e-saude-no-trabalho/caepi/tgg_export_caepi.txt`

---

## 🚀 SUBINDO NO RAILWAY

### 1. Compilar localmente (opcional)
```bash
mvn clean package -DskipTests
```

### 2. Push para GitHub
```bash
git init
git add .
git commit -m "CA-API Unomed v1.0"
git remote add origin https://github.com/seu-usuario/ca-api.git
git push -u origin main
```

### 3. No Railway
- New Project → Deploy from GitHub
- Selecionar o repositório `ca-api`
- Adicionar variável de ambiente:
  - `RELOAD_KEY` = (sua senha forte aqui)
- Deploy automático!

---

## 📡 ENDPOINTS

### Consultar CA pelo número
```
GET /ca/12345
```
**Resposta:**
```json
{
  "numeroCa": "12345",
  "descricao": "LUVA DE RASPA BOVINA",
  "fabricante": "EPI Brasil Ltda",
  "cnpj": "12.345.678/0001-90",
  "dataValidade": "28/08/2026",
  "status": "VÁLIDO",
  "tipoEpi": "Proteção das mãos",
  "nrAprovacao": "NR-06"
}
```

### Buscar por nome do EPI
```
GET /ca/buscar?nome=luva
```

### Ver status da base
```
GET /ca/status
```
**Resposta:**
```json
{
  "status": "online",
  "totalRegistros": 52000,
  "ultimaAtualizacao": "13/03/2025 20:15:00",
  "statusUltimaCarga": "OK",
  "atualizacaoAutomatica": "Todo dia às 20h15"
}
```

### Forçar atualização manual
```
GET /ca/reload?key=suasenha
```

---

## 🔧 INTEGRAÇÃO COM GESTOR SST (Java)

```java
// No seu serviço do Gestor SST
String url = "https://ca-api.railway.app/ca/" + numeroCa;
RestTemplate rest = new RestTemplate();
CaDTO ca = rest.getForObject(url, CaDTO.class);

if (ca != null && "VÁLIDO".equals(ca.getStatus())) {
    // CA válido — pode usar
} else {
    // CA vencido ou não encontrado — alertar usuário
}
```

---

## ⚙️ VARIÁVEIS DE AMBIENTE

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `PORT` | 8080 | Porta da aplicação (Railway define automaticamente) |
| `RELOAD_KEY` | unomed2025 | Senha para endpoint /ca/reload — TROQUE! |

---

## ⚠️ OBSERVAÇÃO SOBRE O LAYOUT DO ARQUIVO

O arquivo do MTE pode mudar o layout das colunas eventualmente.  
Se os dados voltarem errados, verificar o cabeçalho nos logs do Railway  
e ajustar o método `parsearLinha()` no `CaService.java`.

Os logs mostram: `🔎 Cabeçalho do arquivo: ...` na primeira carga.

---

**Stepenovski Clínica Médica Ltda - UNOMED**  
CNPJ: 11.779.877/0001-49 | (42) 3232-2273
