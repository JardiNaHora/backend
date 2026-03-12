# Análise do Projeto JardiNaHora - Comparação com Requisitos e Regras de Negócio

## Resumo Executivo

Esta análise compara a implementação atual do projeto JardiNaHora com:

- **Requisitos Funcionais (RF)**
- **Regras de Negócio (RN)**
- **Requisitos Não Funcionais (RNF)**

Também aponta o que já foi feito, o que falta, e as opções tecnológicas adotadas, considerando o contexto de **projeto acadêmico com recursos gratuitos**.

---

## 1. Tecnologias e Opções Adotadas

### 1.1. Backend

- **Spring Boot 3.1.5**
- **Java 17**
- **PostgreSQL**
- **Spring Security** com:
  - Login com usuário/senha
  - OAuth2 (Google e GitHub)
- **Spring Data JPA**
- **Spring Boot Mail** (Gmail – senha de app)
- **HATEOAS**
- **Bean Validation**
- **Thymeleaf** (templates de e-mail)
- **Firebase Admin SDK** (FCM – via arquivo ou Base64)
- **Spring Boot Actuator**
- **Spring Cache**
- **SSE (Server-Sent Events)** para atualização em tempo real

### 1.2. Frontend

- React (Create React App)
- React Router DOM
- Redux Toolkit
- Material UI
- **Leaflet + OpenStreetMap** para mapas
- Integração via `REACT_APP_BACKEND_URL` com o backend

---

## 2. Regras de Negócio (RN)

### RN01 – Funcionamento da Jardineira (Seg–Sex, 7h–21h55)

- ✅ Implementado:
  - Validador `@ValidOperatingHours`
  - Aplicado em `TravelDTO` (início e término)
  - Verifica dia útil e faixa horária

### RN02 – Capacidade de Transporte (30–40 pessoas)

- ✅ Implementado:
  - `VehicleDTO.passengers` com `@Min(30)` e `@Max(40)`

### RN03 – Trajeto Pré-definido

- ✅ Implementado:
  - Enum `Route` com rotas válidas
  - Anotação `@ValidRoute` em `TripDTO.route`

### RN04 – E-mail válido e senha ≥ 6 caracteres

- ✅ Implementado completamente:
  - `UserDTO.username` com `@Email`
  - `UserDTO.password` com `@Size(min = 6)`

### RN05 – Confirmação de cadastro por e-mail

- ✅ Implementado completamente:
  - `User.emailConfirmed`, `confirmationToken`, `tokenExpiration`
  - Serviço `EmailService` com envio de e-mail HTML (Thymeleaf)
  - `UserServiceImpl` gera token, desabilita conta e envia e-mail
  - Endpoints:
    - `GET /user/confirm-email?token=...`
    - `POST /user/resend-confirmation?email=...`

### RN06 – Notificações Push (Opcional)

- ✅ Implementado completamente:
  - `Notification` e `DeviceToken`
  - `PushNotificationService` + `NotificationController`
  - Lógica de proximidade (Haversine)
  - Integração com FCM (quando `FIREBASE_ENABLED=true`)

### RN07 – Dados Pré-definidos de Horários de Viagens

- ⚠️ Parcial:
  - `Trip` e `Travel` existem
  - **Falta** entidade específica para horários pré-definidos (`Schedule`/`DailySchedule`)

### RN08 – Quantidade de Viagens (Inteiro ≥ 0)

- ✅ Implementado:
  - `TravelDTO.distanceTraveled` e `numberOfTrips` com `@Min(0)`

### RN09 – Percurso (String identificando trajeto e sentido)

- ✅ Implementado:
  - `Trip.route` validado com `@ValidRoute`

### RN10 – Formato de Data/Hora (DD/MM/AAAA HH:MM)

- ✅ Implementado completamente:
  - `DateTimeConverter` com múltiplos formatos aceitos
  - `@ValidDateTimeFormat` aplicado em `TravelDTO`

### RN11 – Validação de Dados

- ✅ Implementado completamente:
  - Uso sistemático de `@Valid` e anotações padrão
  - Validadores customizados
  - `GlobalException` para tratamento padronizado

---

## 3. Requisitos Funcionais (RF)

### RF01 – Visualização de Mapa com Trajeto e Posição Atual

- ✅ Backend:
  - `GET /vehicle/{id}/current-location`
  - `GET /vehicle/{id}/location-history`
  - `GET /vehicle/all/current-locations`
  - DTO `VehicleLocationDTO` com status (`online/stale/offline`)
- ✅ Frontend:
  - Mapa com **Leaflet + OpenStreetMap**
  - Consome `/vehicle/all/current-locations`
- 🔜 Futuro:
  - Mostrar trilhas/rotas completas com base no histórico (opcional)

### RF02 – Atualização de Posição em Tempo Real

- ✅ Backend:
  - `POST /api/embedded-system/data`
  - `GET /api/realtime/subscribe` (SSE)
  - `POST /api/realtime/unsubscribe`
  - `GET /api/realtime/stats`
- ✅ Frontend (parcial):
  - Polling periódico das localizações
- 🔜 Futuro:
  - Integrar SSE no frontend para real time sem polling

### RF03 – Informações Adicionais do Veículo (Previsão de Chegada)

- ✅ Backend:
  - `ArrivalEstimateDTO`
  - `GET /vehicle/{id}/arrival-estimate`
    - Usa posição atual, distância (Haversine) e velocidade média recente
- 🔜 Frontend:
  - Tela/UX específica para mostrar ETA ao usuário

### RF04 – Cadastro e Login

- ✅ Backend:
  - CRUD `User`
  - `UserService.registerAccount`
  - Login com Spring Security + OAuth2 (Google/GitHub)
- ✅ Frontend:
  - Login Google funcional (`/oauth2/authorization/google`)

### RF05 – Notificações ao Usuário

- ✅ Backend:
  - `POST /api/notifications/device-token`
  - `DELETE /api/notifications/device-token`
  - `PUT /api/notifications/user-location`
  - `GET /api/notifications` (paginado)
  - `GET /api/notifications/unread-count`
  - `POST /api/notifications/{id}/read`
- 🔜 Frontend:
  - Tela de notificações, badge de não lidas e integração com FCM Web (opcional)

### RF06 – Relatórios Administrativos

- ✅ Backend:
  - `ReportService` + `ReportController`
  - Endpoints `/admin/reports/*` com:
    - Velocidade média por veículo/período
    - Quantidade de viagens
    - Distância mensal
    - Comparações básicas
- 🔜 Futuro:
  - Exportação para PDF/Excel
  - Telas de visualização com gráficos/tabelas

---

## 4. Requisitos Não Funcionais (RNF)

### RNF01 – Desempenho e Escalabilidade

- ✅ Medidas atuais:
  - Uso de cache (`Spring Cache`) para dados de leitura frequente (locais, relatórios simples)
  - Atualizações em tempo real via SSE (evita polling agressivo no futuro)
  - Consultas com paginação (`Pageable`) em listagens

### RNF02 – Segurança

- ✅ Implementado:
  - Login com senha + OAuth2 (Google/GitHub)
  - Proteção de rotas por perfil (`USER`, `ADMIN`)
  - Endpoints públicos bem delimitados (`/login`, `/oauth2/**`, `/actuator/health`, `/actuator/info`, `/api/embedded-system/data`, `/user/confirm-email`, `/user/resend-confirmation`)
- 🔜 Futuro:
  - Chave de API para ESP32 (autenticar o dispositivo)
  - Rate limiting em endpoints críticos

### RNF03 – Monitoramento e Observabilidade

- ✅ Implementado:
  - `spring-boot-starter-actuator`
  - `/actuator/health` e `/actuator/info` abertos
  - Outros endpoints de Actuator para monitoramento interno

---

## 5. Sistema Embarcado (ESP32) e Integração

### 5.1. Endpoints dedicados

- ✅ Implementados:
  - `POST /api/embedded-system/data`
    - Recebe dados de latitude, longitude, horário e veículo
  - `GET /api/embedded-system/vehicle/{vehicleId}/latest-location`
  - `GET /api/embedded-system/vehicle/{vehicleId}/latest-location-formatted`

### 5.2. Fluxo de dados

1. ESP32 envia posição periódica para `/api/embedded-system/data`.
2. Backend salva em `EmbeddedSystem`/`Travel` e registra histórico.
3. Backend dispara:
   - `PushNotificationService.checkProximityAndNotify` (notificações por proximidade).
   - `RealtimeUpdateService.broadcastVehicleLocationUpdate` (SSE).

### 5.3. Próximos passos recomendados

- Adicionar autenticação simples para o ESP32 (por exemplo, header com API key).
- Ajustar taxa de envio da ESP32 para equilibrar precisão e uso de recursos.

---

## 6. Situação Atual por Camada

### 6.1. Backend – Situação Geral

- ✅ **Concluído / Estável**
  - Regras de negócio principais (RN01–RN06, RN08–RN11)
  - Sistema de confirmação de e-mail
  - Notificações push + fallback em banco
  - Endpoints de relatórios administrativos
  - Atualização em tempo real via SSE
  - Integração com FCM e Gmail (recursos gratuitos)
  - Paginação em listagens principais
  - Cache e Actuator
  - Inicialização de dados de roles (DataInitializer)

- ⚠️ **Parcial / Melhorias futuras**
  - RN07 – entidade de horários pré-definidos
  - Exportação de relatórios (PDF/Excel)
  - Endpoints mais ricos de comparação de performance entre veículos
  - Endurecimento de segurança (API key ESP32, rate limiting)

### 6.2. Frontend – Situação Geral

- ✅ **Concluído / Integrado parcialmente**
  - Cliente HTTP centralizado (`api.js`) com `REACT_APP_BACKEND_URL`
  - Mapa com **Leaflet + OpenStreetMap**
  - Consumo de `/vehicle/all/current-locations` (polling)
  - Login com Google (redireciona para backend OAuth2)

- ⚠️ **Pontos pendentes**
  - Integração com SSE (`/api/realtime/subscribe`) para atualização em tempo real
  - Telas de:
    - Confirmação de e-mail / reenvio
    - Notificações (lista, badge de não lidas, “marcar como lida”)
    - Relatórios administrativos (gráficos/tabelas)
  - Ajuste de formulários para:
    - Formato de data/hora `DD/MM/AAAA HH:MM`
    - Faixa de horário de funcionamento
    - Dropdown com rotas (`Route`)
    - Limites de capacidade (30–40 passageiros) com feedback visual

---

## 7. Próximos Passos Recomendados

### 7.1. Alta Prioridade (Acadêmico / Recursos Gratuitos)

- **Backend**
  - Consolidar o uso de serviços gratuitos:
    - Garantir que FCM está configurado via **arquivo ou Base64** (já previsto).
    - Manter Gmail com senha de app para confirmação de e-mail.
    - Usar apenas **OpenStreetMap** para mapas (já adotado).
  - Adicionar API key simples para ESP32 (token fixo em variável de ambiente).

- **Frontend**
  - Criar telas simples para:
    - Confirmação de e-mail (mensagem amigável após clique no link).
    - Reenvio de e-mail de confirmação.
    - Notificações (lista, badge, marcar como lida).
  - Ajustar formulários para seguir as regras de negócio (datas, horários, rotas).

### 7.2. Média Prioridade

- **Backend**
  - Implementar entidade `Schedule`/`DailySchedule` para RN07
    - Horários padrão por dia da semana/rota
    - Endpoints para consultar esses horários
  - Endpoints de comparação de performance entre veículos:
    - Comparar velocidade média e quantidade de viagens no mesmo período

- **Frontend**
  - Tela de relatórios administrativos:
    - Consumo de `/admin/reports/*`
    - Exibição em tabelas e gráficos

### 7.3. Baixa Prioridade / Evoluções Futuras

- Exportação de relatórios em PDF/Excel.
- Internacionalização (pt-BR/en-US).
- Tema escuro e ajustes avançados de UX.

---

## 8. Conclusão

- **Backend**: ~90–95% dos requisitos e regras de negócio **já implementados**, com foco em recursos gratuitos (Firebase, Gmail, OpenStreetMap).
- **Frontend**: base pronta, mas ainda precisa de **integrações de UX** com:
  - SSE (tempo real)
  - Notificações
  - Relatórios
  - Ajustes de formulários conforme regras de negócio.

Para fins acadêmicos, o estado atual é **muito avançado**: o que falta está mais ligado à experiência do usuário e refinamentos do que a lacunas críticas de arquitetura.

