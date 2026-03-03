package security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Service de gestion des tokens API
 * - Génère des tokens DÉTERMINISTES basés sur la date du jour
 * - Le même token est généré pour une même date
 * - Validité paramétrable (par défaut 7 jours)
 */
public class TokenService {
    
    // Durée de validité par défaut en jours
    private static int TOKEN_VALIDITY_DAYS = 7;
    
    // Clé secrète pour la génération des tokens (à personnaliser)
    private static final String SECRET_KEY = "VotreCleSuperSecrete2026!";
    
    // Singleton
    private static TokenService instance;
    
    private TokenService() {}
    
    public static synchronized TokenService getInstance() {
        if (instance == null) {
            instance = new TokenService();
        }
        return instance;
    }
    
    /**
     * Configure la durée de validité des tokens
     * @param days nombre de jours de validité
     */
    public void setTokenValidityDays(int days) {
        if (days > 0) {
            TOKEN_VALIDITY_DAYS = days;
        }
    }
    
    /**
     * Obtient la durée de validité actuelle
     */
    public int getTokenValidityDays() {
        return TOKEN_VALIDITY_DAYS;
    }
    
    /**
     * Génère le token du jour (déterministe - même token pour la même date)
     * @return le token pour aujourd'hui
     */
    public String generateTodayToken() {
        return generateTokenForDate(LocalDate.now());
    }
    
    /**
     * Génère le token pour une date donnée
     * @param date la date
     * @return le token pour cette date
     */
    public String generateTokenForDate(LocalDate date) {
        String rawToken = date.format(DateTimeFormatter.ISO_DATE) + "|" + SECRET_KEY;
        return hashAndEncode(rawToken);
    }
    
    /**
     * Valide un token - vérifie s'il correspond à un token valide des X derniers jours
     * @param token le token à valider
     * @return true si le token est valide
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Enlève le préfixe "Bearer " si présent
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // Vérifie si le token correspond à un des tokens valides des X derniers jours
        LocalDate today = LocalDate.now();
        for (int i = 0; i < TOKEN_VALIDITY_DAYS; i++) {
            LocalDate date = today.minusDays(i);
            String validToken = generateTokenForDate(date);
            if (validToken.equals(token)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Obtient la date d'expiration du token actuel
     */
    public LocalDate getExpirationDate() {
        return LocalDate.now().plusDays(TOKEN_VALIDITY_DAYS);
    }
    
    /**
     * Hash et encode une chaîne en Base64 URL-safe
     */
    private String hashAndEncode(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * Méthode main pour générer et afficher le token du jour
     */
    public static void main(String[] args) {
        TokenService service = getInstance();
        
        // Permet de configurer la validité via argument
        if (args.length > 0) {
            try {
                service.setTokenValidityDays(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        String token = service.generateTodayToken();
        LocalDate today = LocalDate.now();
        LocalDate expiration = service.getExpirationDate();
        
        System.out.println("========================================");
        System.out.println("       TOKEN API DU JOUR");
        System.out.println("========================================");
        System.out.println("Date:       " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        System.out.println("Expire le:  " + expiration.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        System.out.println("Validite:   " + service.getTokenValidityDays() + " jours");
        System.out.println("----------------------------------------");
        System.out.println("TOKEN: " + token);
        System.out.println("----------------------------------------");
        System.out.println("Usage: Authorization: Bearer " + token);
        System.out.println("========================================");
    }
}
