package controller;

import dao.LieuDao;
import dao.ReservationDao;
import model.Lieu;
import model.Reservation;
import myframework.annotation.GET;
import myframework.annotation.Json;
import myframework.annotation.MyController;
import myframework.annotation.MyMapping;
import myframework.annotation.POST;
import myframework.annotation.RequestParam;
import myframework.fw.ModelView;
import security.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MyController
public class ReservationController {
    private final LieuDao lieuDao = new LieuDao();
    private final ReservationDao reservationDao = new ReservationDao();
    private final TokenService tokenService = TokenService.getInstance();
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @MyMapping("/")
    @GET
    public ModelView home() throws Exception {
        return showForm();
    }

    @MyMapping("/reservations/new")
    @GET
    public ModelView showForm() throws Exception {
        ModelView mv = new ModelView("views/reservation-form.jsp");
        List<Lieu> hotels = lieuDao.findHotels();
        mv.addAttribute("hotels", hotels);
        return mv;
    }

    @MyMapping("/reservations")
    @POST
    public ModelView createReservation(
        @RequestParam(value = "idClient", required = true) String idClient,
        @RequestParam(value = "nbpassagers", required = true) int nbpassagers,
        @RequestParam(value = "dateheure", required = true) String dateheure,
        @RequestParam(value = "idLieu", required = true) int idLieu
    ) throws Exception {
        ModelView mv = new ModelView("views/reservation-form.jsp");
        List<Lieu> hotels = lieuDao.findHotels();
        mv.addAttribute("hotels", hotels);

        if (!idClient.matches("^[A-Za-z0-9]{4}$")) {
            mv.addAttribute("error", "L'identifiant client doit contenir exactement 4 caractères alphanumériques.");
            return mv;
        }

        if (nbpassagers <= 0) {
            mv.addAttribute("error", "Le nombre de passagers doit être supérieur à 0.");
            return mv;
        }

        Timestamp ts;
        try {
            LocalDateTime ldt = LocalDateTime.parse(dateheure, inputFormatter);
            ts = Timestamp.valueOf(ldt);
        } catch (Exception ex) {
            mv.addAttribute("error", "Format de date/heure invalide.");
            return mv;
        }
 
        reservationDao.insert(idClient, nbpassagers, ts, idLieu);

        ModelView success = new ModelView("views/reservation-success.jsp");
        success.addAttribute("message", "Réservation enregistrée avec succès.");
        return success;
    }

    @MyMapping("/api/reservations")
    @GET
    @Json
    public Object listReservations(HttpServletRequest request) throws Exception {
        // Vérifie le token d'authentification depuis le header Authorization
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || authHeader.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("data", null);
            error.put("status", "error");
            error.put("code", 401);
            error.put("count", 0);
            error.put("message", "Token manquant. Header 'Authorization: Bearer <token>' requis.");
            return error;
        }
        
        // Vérifie le format "Bearer <token>"
        if (!authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("data", null);
            error.put("status", "error");
            error.put("code", 401);
            error.put("count", 0);
            error.put("message", "Format invalide. Utilisez 'Authorization: Bearer <token>'.");
            return error;
        }
        
        String token = authHeader.substring(7); // Enlève "Bearer "
        
        if (!tokenService.validateToken(token)) {
            Map<String, Object> error = new HashMap<>();
            error.put("data", null);
            error.put("status", "error");
            error.put("code", 401);
            error.put("count", 0);
            error.put("message", "Token invalide ou expire.");
            return error;
        }
        
        try {
            List<Reservation> reservations = reservationDao.findAll();
            Map<String, Object> result = new HashMap<>();
            result.put("data", reservations);
            result.put("status", "success");
            result.put("code", 200);
            result.put("count", reservations.size());
            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("data", null);
            error.put("status", "error");
            error.put("code", 500);
            error.put("count", 0);
            error.put("message", e.getMessage());
            return error;
        }
    }
}
