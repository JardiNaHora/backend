package com.jardinahora.backend.services;

import java.util.List;

/**
 * Serviço para envio de notificações push via Firebase Cloud Messaging (FCM).
 * Plano gratuito: até 10.000 mensagens/mês.
 *
 * Para ativar: configurar app.firebase.enabled=true e colocar o JSON de
 * credenciais em app.firebase.credentials-path (caminho do arquivo) ou
 * usar variável de ambiente FIREBASE_CREDENTIALS_JSON (conteúdo base64).
 */
public interface FCMService {

    /**
     * Verifica se o FCM está configurado e disponível.
     */
    boolean isEnabled();

    /**
     * Envia notificação push para um único token de dispositivo.
     *
     * @param deviceToken Token FCM do dispositivo
     * @param title       Título da notificação
     * @param body        Corpo da mensagem
     * @param data        Dados opcionais (ex: vehicleId, type)
     * @return true se enviado com sucesso
     */
    boolean sendToToken(String deviceToken, String title, String body, java.util.Map<String, String> data);

    /**
     * Envia notificação para múltiplos tokens (ex: todos os dispositivos de um usuário).
     *
     * @param deviceTokens Lista de tokens FCM
     * @param title        Título
     * @param body         Corpo
     * @param data         Dados opcionais
     * @return Quantidade de envios bem-sucedidos
     */
    int sendToTokens(List<String> deviceTokens, String title, String body, java.util.Map<String, String> data);
}
