package com.jardinahora.backend.utils;

/**
 * Utilitário para cálculos de distância geográfica
 * 
 * Implementa a fórmula de Haversine para calcular distância entre dois pontos GPS
 */
public class DistanceCalculator {
    
    /**
     * Raio médio da Terra em quilômetros
     */
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calcula a distância em quilômetros entre dois pontos GPS usando a fórmula de Haversine
     * 
     * @param lat1 Latitude do primeiro ponto
     * @param lon1 Longitude do primeiro ponto
     * @param lat2 Latitude do segundo ponto
     * @param lon2 Longitude do segundo ponto
     * @return Distância em quilômetros
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Converte graus para radianos
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        // Fórmula de Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Calcula a distância em metros entre dois pontos GPS
     * 
     * @param lat1 Latitude do primeiro ponto
     * @param lon1 Longitude do primeiro ponto
     * @param lat2 Latitude do segundo ponto
     * @param lon2 Longitude do segundo ponto
     * @return Distância em metros
     */
    public static double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2) * 1000;
    }
    
    /**
     * Verifica se dois pontos estão dentro de uma distância específica
     * 
     * @param lat1 Latitude do primeiro ponto
     * @param lon1 Longitude do primeiro ponto
     * @param lat2 Latitude do segundo ponto
     * @param lon2 Longitude do segundo ponto
     * @param maxDistanceKm Distância máxima em quilômetros
     * @return true se estiverem dentro da distância máxima
     */
    public static boolean isWithinDistance(double lat1, double lon1, double lat2, double lon2, double maxDistanceKm) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= maxDistanceKm;
    }
}
