# Ações Necessárias do Usuário (Projeto Acadêmico – Recursos Gratuitos)

Este documento lista o que **você precisa fazer** para deixar o backend 100% operacional com serviços externos **gratuitos**. As opções adotadas no projeto estão indicadas abaixo.

---

## 1. Firebase (FCM) – Notificações push reais | **Opção B (Base64) adotada**

**O que já está pronto:**  
O backend já envia notificações para o FCM quando está configurado. Se não configurar, as notificações continuam sendo **salvas no banco** e exibidas no app; apenas o push no celular não será enviado.

**Você já fez:** baixou o JSON e colocou em `backend/firebase-credentials.json`.

**Para seguir com a Opção B (variável em Base64):**

1. Converta o conteúdo do arquivo `firebase-credentials.json` para Base64:

   - **Windows (PowerShell)**:

     ```powershell
     cd "C:\Users\guilherme.silva.TCE\Desktop\Projetos\JardiNaHora\backend"
     [Convert]::ToBase64String([IO.File]::ReadAllBytes("firebase-credentials.json"))
     ```

   - **Linux/Mac:**

     ```bash
     base64 -i firebase-credentials.json   # ou base64 -w 0 firebase-credentials.json
     ```

   - Ou use um conversor online (cuidado: não use em computadores públicos).

2. Defina as variáveis de ambiente (ou no `.env` do backend):

   ```env
   FIREBASE_ENABLED=true
   FIREBASE_CREDENTIALS_JSON_BASE64=<resultado_do_base64>
   FIREBASE_CREDENTIALS_PATH=
   ```

3. O arquivo `firebase-credentials.json` pode permanecer local para conversão; **não faça commit** dele (já está no `.gitignore`).

**Alternativa – Opção A (arquivo):** se preferir usar o arquivo diretamente:

```env
FIREBASE_ENABLED=true
FIREBASE_CREDENTIALS_PATH=file:./firebase-credentials.json
FIREBASE_CREDENTIALS_JSON_BASE64=
```

**Limite:** até 10.000 mensagens/mês.

---

## 2. E-mail (confirmação de cadastro) | **Opção A (Gmail) adotada**

**O que já está pronto:**  
O serviço de e-mail está implementado e o perfil de desenvolvimento já usa Gmail (`smtp.gmail.com`, porta `587`).

**Você já fez:** criou a senha de app do Gmail.

**O que falta configurar:** defina as variáveis de ambiente (ou no seu `.env`):

```env
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=<senha_de_app_gerada>
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
```

> A senha de app é gerada em:  
> Conta Google → Segurança → Verificação em duas etapas → Senhas de app.

**Limite:** ~500 e-mails/dia.

---

## 3. Actuator (health e métricas)

**O que já está pronto:**  
O Actuator está no projeto e expõe:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

No perfil `dev`, o health de mail está desabilitado para evitar falhas caso o SMTP esteja bloqueado pela rede.

**Ação sua:** nenhuma obrigatória. Você pode consumir esses endpoints em uma tela de “status do sistema” no futuro.

---

## 4. Banco de dados (PostgreSQL)

**O que já está pronto:**  
Uso de PostgreSQL configurado no projeto. O Hibernate (`ddl-auto: update`) cria/atualiza as tabelas automaticamente.

**O que você precisa fazer:**

1. Criar o banco (vazio), por exemplo `jardinahora`.
2. Definir no `.env` do backend:

```env
DEV_DB_HOST=localhost
DEV_DB_PORT=5432
DEV_DB_NAME=jardinahora
DEV_DB_USERNAME=postgres
DEV_DB_PASSWORD=sua_senha
```

3. Garantir que o serviço PostgreSQL (ou container Docker) está rodando.

---

## 5. Mapas no frontend | **OpenStreetMap adotado**

**Escolha do projeto:** **OpenStreetMap** com **Leaflet** (alternativa 100% gratuita ao Google Maps).

**Onde é usado:** no **frontend**, para exibir mapa e trajetos. O backend não faz chamadas a APIs de mapas; apenas fornece latitude/longitude.

**O que você já fez:** criou conta no OpenStreetMap (útil para contribuir com o mapa; não é obrigatório para exibir mapas no app).

**O que o frontend deve usar:**

- **Leaflet** + **OpenStreetMap** (tiles gratuitos, sem chave de API).
- Exemplo de tile:  

  ```txt
  https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png
  ```

**Backend:** já fornece:

- `/vehicle/all/current-locations`
- `/vehicle/{id}/current-location`
- `/vehicle/{id}/location-history`
- `/vehicle/{id}/arrival-estimate`

O frontend só precisa consumir e plotar os pontos no mapa.

---

## 6. Resumo rápido

| Item           | Opção adotada        | Ação sua                                      | Custo  |
|----------------|----------------------|-----------------------------------------------|--------|
| PostgreSQL     | -                    | Configurar variáveis DEV_DB_*                 | Grátis |
| E-mail         | **Opção A – Gmail**  | `MAIL_USERNAME` e `MAIL_PASSWORD` (senha de app) | Grátis |
| Firebase (FCM) | **Opção B – Base64** | `FIREBASE_ENABLED=true` e `FIREBASE_CREDENTIALS_JSON_BASE64` | Grátis |
| Actuator       | -                    | Nada (já configurado)                         | Grátis |
| Mapas          | **OpenStreetMap**    | Frontend usa Leaflet + OSM; sem API key       | Grátis |

---

## 7. Variáveis de ambiente sugeridas (exemplo consolidado)

```env
# Banco
DEV_DB_HOST=localhost
DEV_DB_PORT=5432
DEV_DB_NAME=jardinahora
DEV_DB_USERNAME=postgres
DEV_DB_PASSWORD=sua_senha

# Frontend (URL do app)
FRONTEND_URL=http://localhost:3000

# E-mail – Opção A (Gmail, senha de app)
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=senha_de_app

# Firebase – Opção B (Base64 do JSON)
FIREBASE_ENABLED=true
FIREBASE_CREDENTIALS_JSON_BASE64=<conteudo_base64_do_firebase-credentials.json>
FIREBASE_CREDENTIALS_PATH=

# OAuth (login social) – opcionais
GOOGLE_CLIENT_ID=dummy-google-id
GOOGLE_CLIENT_SECRET=dummy-google-secret
GITHUB_CLIENT_ID=dummy-github-id
GITHUB_CLIENT_SECRET=dummy-github-secret
```

---

Se algo exigir pagamento ou conta paga no futuro, você pode trocar por uma das alternativas gratuitas listadas aqui. Para uso acadêmico, todas as opções atuais são suficientes.

---

## 8. Checklist do que já foi feito e próximos passos

### 8.1. Status atual (ambiente de desenvolvimento)

- **Firebase (FCM)**  
  - ✅ `firebase-credentials.json` salvo em `backend/firebase-credentials.json`.  
  - ✅ Conteúdo convertido para Base64 e atribuído à variável `FIREBASE_CREDENTIALS_JSON_BASE64`.  
  - ✅ `FIREBASE_ENABLED=true` definido no `.env`.  
  - ✅ `FIREBASE_CREDENTIALS_PATH=file:./firebase-credentials.json` configurado.  
  - ✅ Dependências `firebase-admin` já presentes no `pom.xml`.  
  - ▶️ **Situação**: backend pronto para enviar notificações reais via FCM em ambiente de desenvolvimento.

- **E-mail (Gmail)**  
  - ✅ Senha de app do Gmail criada.  
  - ✅ Variáveis `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` preenchidas e testadas.  
  - ▶️ **Situação**: serviço de e-mail funcional para confirmação de cadastro em desenvolvimento.

- **Actuator**  
  - ✅ Dependência adicionada e endpoints principais (`/actuator/health`, `/actuator/info`, `/actuator/metrics`) expostos.  
  - ✅ Health de `mail` desabilitado em `dev` para evitar ruído em caso de falha de SMTP.  
  - ▶️ **Situação**: nada obrigatório a fazer neste momento; usar apenas se quiser monitorar a aplicação.

- **Banco de dados (PostgreSQL)**  
  - ✅ Banco criado via IntelliJ e conectado.  
  - ✅ Variáveis `DEV_DB_*` configuradas no `.env`.  
  - ✅ Aplicação sobe criando/atualizando as tabelas automaticamente.  
  - ▶️ **Situação**: ambiente de banco de dados pronto para desenvolvimento e testes.

### 8.2. O que pode ser feito agora (próximos passos práticos)

- **Testes integrados das notificações**  
  - Registrar um dispositivo (token FCM) via endpoint `/api/notifications/device-token`.  
  - Configurar um ponto de embarque do usuário com `/api/notifications/user-location`.  
  - Simular/envio de localização de veículo pelo endpoint do sistema embarcado (`/api/embedded-system/data`) e verificar se:
    - notificações são geradas no banco (`Notification`);
    - mensagens push chegam ao dispositivo (se o app/web estiver integrado ao FCM).

- **Validação do fluxo de confirmação de e-mail**  
  - Criar um usuário novo via fluxo de cadastro.  
  - Confirmar se o e-mail de confirmação chega na caixa de entrada.  
  - Testar:
    - clique no link de confirmação (`/user/confirm-email?token=...`);  
    - reenvio de confirmação (`/user/resend-confirmation`).

- **Integração do frontend com o backend**  
  - Garantir que o frontend esteja usando `REACT_APP_BACKEND_URL=http://localhost:8080`.  
  - Consumir:
    - `/vehicle/all/current-locations` no mapa;  
    - endpoints de histórico (`/vehicle/{id}/location-history`) e de previsão (`/vehicle/{id}/arrival-estimate`) em telas adequadas.

- **Opcional – Monitoramento com Actuator**  
  - Criar, no futuro, uma tela simples de “status do sistema” no frontend, consumindo `/actuator/health` e `/actuator/info`.  
  - Útil para apresentação do projeto e para demonstrar boas práticas de observabilidade.


