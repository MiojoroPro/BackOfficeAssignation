package security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Filtre d'authentification pour protéger les endpoints API
 * Vérifie la présence et la validité du token dans le header Authorization
 * Le token est généré par rapport à la date du jour et valide pendant X jours
 */
public class ApiAuthFilter implements Filter {
    
    private TokenService tokenService;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        tokenService = TokenService.getInstance();
        
        // Configuration de la durée de validité depuis web.xml
        String validityDays = filterConfig.getInitParameter("tokenValidityDays");
        if (validityDays != null && !validityDays.isEmpty()) {
            try {
                tokenService.setTokenValidityDays(Integer.parseInt(validityDays));
            } catch (NumberFormatException e) {
                // Garde la valeur par défaut (7 jours)
            }
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Récupère le chemin de la requête
        String path = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = path.substring(contextPath.length());
        
        // Ne protège que les URLs qui commencent par /api/
        if (!relativePath.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Récupère le token depuis le header Authorization
        String authHeader = httpRequest.getHeader("Authorization");
        
        if (authHeader == null || authHeader.isEmpty()) {
            sendUnauthorizedResponse(httpResponse, "Token manquant. Header 'Authorization' requis.");
            return;
        }
        
        // Valide le token (vérifie si c'est un token valide des X derniers jours)
        if (!tokenService.validateToken(authHeader)) {
            sendUnauthorizedResponse(httpResponse, "Token invalide ou expire.");
            return;
        }
        
        // Token valide, continue la chaîne
        chain.doFilter(request, response);
    }
    
    /**
     * Envoie une réponse 401 Unauthorized en JSON
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        writer.write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
        writer.flush();
    }
    
    @Override
    public void destroy() {
        // Nettoyage si nécessaire
    }
}
