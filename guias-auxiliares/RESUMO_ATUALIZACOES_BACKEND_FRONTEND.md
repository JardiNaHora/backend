# Resumo das Atualizações – Backend e Frontend

Este arquivo resume as principais mudanças feitas desde a versão originalmente publicada no GitHub, separando **backend** e **frontend**, e destacando: o que era necessário, o que foi feito e o que ainda falta.

---

## 1. Backend

### 1.1. Pontos principais implementados

- **Integração com sistema embarcado (ESP32)**  
  - Modelo `EmbeddedSystem` ajustado (relacionado a `Vehicle`).  
  - `EmbeddedSystemDTO` para receber dados (GPS, giroscópio, acelerômetro, timestamp).  
  - `EmbeddedSystemRepository` com consultas por veículo e data.  
  - `EmbeddedSystemService`/`EmbeddedSystemServiceImpl` com:
    - Validações de dados.  
    - Ligação com veículos por ID/nome/placa.  
    - Persistência dos dados recebidos.  
  - `EmbeddedSystemController` com `POST /api/embedded-system/data` (público).  

- **Localização atual e histórico de veículos (RF01/RF02)**  
  - DTO `VehicleLocationDTO` com:
    - `latitude`, `longitude`, `gpsPosition`  
    - sensores, `lastUpdate`, `secondsSinceLastUpdate`, `status` (`online/stale/offline`).  
  - `EmbeddedSystemService` com métodos:
    - `getLatestLocationDTOByVehicleId`  
    - `getRecentLocationsByVehicleId`  
    - `getAllActiveVehicleLocations`  
  - `VehicleController`:
    - `GET /vehicle/{id}/current-location`  
    - `GET /vehicle/{id}/location-history?limit=`  
    - `GET /vehicle/all/current-locations`  

- **Atualização em tempo real (SSE – RF02)**  
  - `RealtimeUpdateService` / `RealtimeUpdateServiceImpl` usando **Server-Sent Events**.  
  - `RealtimeUpdateController`:
    - `GET /api/realtime/subscribe` – cliente se conecta para receber eventos.  
    - `POST /api/realtime/unsubscribe` – encerra conexão.  
    - `GET /api/realtime/stats` – número de conexões ativas.  
  - `EmbeddedSystemServiceImpl` chama `realtimeUpdateService.broadcastVehicleLocationUpdate(...)` após salvar novos dados do sistema embarcado (quando FCM e SSE estão habilitados).  

- **Validações de regras de negócio (RN01–RN11)**  
  - `DateTimeConverter` + anotações/validators customizados:  
    - `@ValidDateTimeFormat`, `@ValidOperatingHours`, `@ValidRoute`.  
  - Atualizações nos DTOs:
    - `UserDTO` – `@Email`, `@Size(min = 6)` (RN04).  
    - `VehicleDTO` – `@Min(30)`, `@Max(40)` para passageiros (RN02).  
    - `TravelDTO` – validações de formato de data/hora e horários dentro de 7h–21h55 (RN01, RN10).  
    - `TripDTO` – uso de `@ValidRoute` (RN03).  

- **Confirmação de cadastro por e-mail (RN05)**  
  - Modelo `User`:
    - Campos `emailConfirmed`, `confirmationToken`, `tokenExpiration`.  
  - `EmailService` / `EmailServiceImpl` usando `spring-boot-starter-mail` + Thymeleaf (template HTML).  
  - `UserService` / `UserServiceImpl`:
    - Ao registrar: gera token, desabilita usuário até confirmação, envia e-mail.  
    - Métodos `confirmEmail(token)` e `resendConfirmationEmail(email)`.  
  - `UserController`:
    - `GET /user/confirm-email?token=...`  
    - `POST /user/resend-confirmation?email=...`  
  - `application-*.yml`: propriedades de e-mail configuráveis (host, porta, usuário, senha).  

- **Sistema de notificações push (RF05/RN06)**  
  - Modelos:
    - `DeviceToken` – token por dispositivo (user, tipo, localização, ativo, notificação habilitada).  
    - `Notification` – registros de notificações enviadas (título, mensagem, tipo, user, vehicle, lida/não lida).  
  - Repositórios: `DeviceTokenRepository`, `NotificationRepository`.  
  - DTO: `DeviceTokenDTO` para registro de token.  
  - Util `DistanceCalculator` (Haversine) para proximidade.  
  - Serviço `PushNotificationService` / `PushNotificationServiceImpl`:
    - Registro/remoção de tokens.  
    - Atualização de localização do usuário.  
    - Detecção de proximidade entre veículo e usuário.  
    - Criação de registros de `Notification`.  
  - Integração com **FCM**:  
    - `FCMService` / `FCMServiceImpl` usando `firebase-admin`.  
    - Propriedades em `app.firebase.*` (`enabled`, `credentials-path`, `credentials-json-base64`).  
    - Quando habilitado (`FIREBASE_ENABLED=true`), envia de fato as notificações push para os tokens registrados.  
  - `NotificationController`:
    - `POST /api/notifications/device-token` – registrar token (usuário autenticado).  
    - `DELETE /api/notifications/device-token/{token}` – desativar token.  
    - `PUT /api/notifications/user-location` – atualizar localização do usuário.  
    - `GET /api/notifications` – listar notificações (paginado).  
    - `GET /api/notifications/unread-count` – contagem de não lidas.  
    - `PUT /api/notifications/{id}/read` – marcar notificação como lida.  

- **Relatórios administrativos (RF06)**  
  - DTOs:
    - `AverageSpeedReportDTO`  
    - `TripsCountReportDTO`  
    - `MonthlyDistanceReportDTO`  
    - `VehiclePerformanceReportDTO`  
  - `TravelRepository` com queries de agregação (distância, número de viagens, por data/mês).  
  - `ReportService` / `ReportServiceImpl`:
    - Cálculo de velocidade média, número de viagens, quilometragem mensal, performance completa por veículo.  
  - `ReportController` (apenas ADMIN):
    - `/admin/reports/average-speed`  
    - `/admin/reports/trips-count`  
    - `/admin/reports/monthly-distance`  
    - `/admin/reports/vehicle-performance`  
    - `/admin/reports/monthly-distance/all`  

- **Infraestrutura adicional**  
  - **Actuator**: `spring-boot-starter-actuator` + configuração de health (`/actuator/health`, `/actuator/info`, `/actuator/metrics` – mail health desabilitado em dev).  
  - **Cache**: `spring-boot-starter-cache` + `CacheConfig` (caches `vehicles`, `routes`, `report`).  
  - **Async**: `AsyncConfig` + `@Async` em operações demoradas (notificações/proximidade).  
  - **Segurança** (`SecurityConfig`):
    - Libera `/api/embedded-system/data`, `/user/confirm-email`, `/user/resend-confirmation`.  
    - Restringe `/admin/**` a ADMIN, `/api/notifications/**` e `/api/realtime/**` a USER.  
  - `.env` + `application-*.yml` consolidados para perfis `dev`, `staging`, `prod`.  
  - `DataInitializer` criando automaticamente roles `USER` e `ADMIN` na tabela `TB_USER_ROLES`.  

### 1.2. O que era necessário / foi feito / falta

- **Necessário (da análise original)**  
  - Endpoints para sistema embarcado.  
  - Regras de negócio RN01–RN11.  
  - Confirmação de cadastro por e-mail.  
  - Localização atual + atualização em tempo real.  
  - Notificações push por proximidade.  
  - Relatórios administrativos.  

- **Já foi feito (backend)**  
  - Itens acima todos implementados.  
  - Integração FCM pronta (resta apenas configurar credenciais no `.env`).  
  - Paginação em listagens principais (`/user-all`, `/vehicle`, `/trip`, `/travel`, `/api/notifications`).  
  - Cache em pontos estratégicos (ainda básico).  
  - Arquitetura preparada para uso acadêmico com serviços gratuitos (Gmail, Firebase, OSM).  

- **Ainda falta / melhorias futuras (backend)**  
  - Criar entidade para **horários pré-definidos** (ex.: `Schedule` / `DailySchedule`) para RN07.  
  - Exportação de relatórios para PDF/Excel.  
  - Endpoints para **comparar performance entre veículos**.  
  - Endpoints específicos de “previsão de chegada” mais ricos (hoje há um endpoint básico `GET /vehicle/{id}/arrival-estimate`).  
  - Eventual endurecimento de segurança: API key para ESP32, rate limiting etc.  

---

## 2. Frontend

### 2.1. Pontos principais já ajustados

- **Configuração de acesso ao backend**  
  - Criado `src/services/api.js` com `axios` centralizado:  
    - `baseURL` = `REACT_APP_BACKEND_URL` (ou `http://localhost:8080`).  
    - `withCredentials: true` (para sessões).  
  - Criado `.env.development` com:  
    - `REACT_APP_BACKEND_URL=http://localhost:8080`.  
  - `AuthService` e outras chamadas passaram a usar `api` em vez de `axios` solto.  

- **Autenticação**  
  - Fluxo de login com Google já apontando para o backend (`/oauth2/authorization/google`).  
  - Tela de Home verifica autenticação via `/home/auth` e redireciona para `/login` se necessário.  

- **Mapa de localização – migração para OpenStreetMap (OSM)**  
  - Substituição do Google Maps (`@react-google-maps/api`) por **Leaflet** (`react-leaflet`).  
  - Novo componente `Home/Map`:
    - Usa `MapContainer` + `TileLayer` com tiles OSM (`https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png`).  
    - Centraliza inicialmente no campus IFCE.  
    - Busca periodicamente (`/vehicle/all/current-locations`) as posições dos veículos (usando `VehicleLocationDTO`).  
    - Mostra marcadores com popup (nome, placa, status).  
    - Opcionalmente mostra a localização atual do usuário (se o navegador permitir).  
  - Remoção da dependência de Google Maps API key e de ThingSpeak (antes usado como “gateway” de posição).  

### 2.2. O que era necessário / foi feito / falta

- **Necessário (da análise original / novo backend)**  
  - Mostrar mapa com trajeto e posição atual em tempo real (usando dados vindos do backend).  
  - Adaptar frontend para regras de negócio (formatos de data/hora, rotas válidas, capacidade etc.).  
  - Integrar notificações (lista de notificações, preferências de push, atualização de localização do usuário).  
  - Criar telas para relatórios administrativos e histórico de viagens.  

- **Já foi feito (frontend)**  
  - Configuração de `REACT_APP_BACKEND_URL` e cliente HTTP unificado.  
  - Login e navegação básica até `/home` funcionais.  
  - Mapa principal migrado para **OpenStreetMap + Leaflet** e consumindo as localizações atuais do backend (via `/vehicle/all/current-locations`).  

- **Ainda falta (frontend)**  
  - **Integração com SSE** (`/api/realtime/subscribe`) para receber atualizações em tempo real sem polling (hoje o mapa faz pooling a cada ~5s).  
  - Tela de **confirmação de e-mail** e de **reenvio de confirmação**, alinhadas com os endpoints `/user/confirm-email` e `/user/resend-confirmation`.  
  - Telas de **notificações**:
    - Listagem (`GET /api/notifications`), badge de não lidas (`/unread-count`), ação “marcar como lida”.  
    - Tela/configuração para ativar/desativar notificações e atualizar ponto de embarque (`PUT /api/notifications/user-location`).  
    - Integração com Firebase Web SDK para registrar tokens FCM (`POST /api/notifications/device-token`) se quiser push Web.  
  - Telas de **relatórios administrativos** (usuário ADMIN) consumindo `/admin/reports/...` e exibindo gráficos/tabelas.  
  - Ajustes de UX para regras de negócio:
    - Inputs de data/hora no formato `DD/MM/AAAA HH:MM`.  
    - Restrições de horário de funcionamento (7h–21h55).  
    - Dropdown com rotas válidas (enum `Route`).  
    - Limites visuais de capacidade (30–40 passageiros).  
  - Revisão de rotas/telas existentes (Histórico Relatórios, Histórico Viagens, etc.) para usar os novos endpoints e formatos do backend.  

---

## 3. Arquivos de documentação relevantes

Para referência, os arquivos mais importantes de documentação agora são:

- `ANALISE_PROJETO_JARDINAHORA.md` – análise completa do projeto vs. requisitos/regas de negócio.  
- `RESUMO_ATUALIZACOES_BACKEND_FRONTEND.md` – **este arquivo**, resumo dos marcos e status atual.  
- `ACOES_NECESSARIAS_USUARIO.md` – como configurar `.env`, FCM (Opção B Base64), Gmail (senha de app), OpenStreetMap etc.  

Os demais arquivos de implementação detalhada (`IMPLEMENTACAO_*.md`, guias específicos de pgAdmin, conexão de banco etc.) podem ser considerados material de apoio/ histórico. Se quiser “enxugar” o repositório para estudo/apresentação, pode mantê-los numa pasta de **documentação antiga** ou removê-los após versionamento em outro lugar, usando este resumo como ponto único de referência.

# Resumo das Atualizações – Backend e Frontend

[conteúdo movido do arquivo original; veja histórico do repositório para detalhes]*** End Patch`"}】`】 ***!
