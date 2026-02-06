package controller;

import dao.HotelDao;
import dao.ReservationDao;
import model.Hotel;
import model.Reservation;
import myframework.annotation.GET;
import myframework.annotation.Json;
import myframework.annotation.MyController;
import myframework.annotation.MyMapping;
import myframework.annotation.POST;
import myframework.annotation.RequestParam;
import myframework.fw.ModelView;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@MyController
public class ReservationController {
    private final HotelDao hotelDao = new HotelDao();
    private final ReservationDao reservationDao = new ReservationDao();
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @MyMapping("/reservations/new")
    @GET
    public ModelView showForm() throws Exception {
        ModelView mv = new ModelView("views/reservation-form.jsp");
        List<Hotel> hotels = hotelDao.findAll();
        mv.addAttribute("hotels", hotels);
        return mv;
    }

    @MyMapping("/reservations")
    @POST
    public ModelView createReservation(
        @RequestParam(value = "idClient", required = true) String idClient,
        @RequestParam(value = "nbpassagers", required = true) int nbpassagers,
        @RequestParam(value = "dateheure", required = true) String dateheure,
        @RequestParam(value = "idHotel", required = true) int idHotel
    ) throws Exception {
        ModelView mv = new ModelView("views/reservation-form.jsp");
        List<Hotel> hotels = hotelDao.findAll();
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

        reservationDao.insert(idClient, nbpassagers, ts, idHotel);

        ModelView success = new ModelView("views/reservation-success.jsp");
        success.addAttribute("message", "Réservation enregistrée avec succès.");
        return success;
    }

    @MyMapping("/api/reservations")
    @GET
    @Json
    public List<Reservation> listReservations() throws Exception {
        return reservationDao.findAll();
    }
}
