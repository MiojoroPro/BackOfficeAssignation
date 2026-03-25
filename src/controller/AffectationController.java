package controller;

import dao.AffectationDao;
import dao.ReservationDao;
import dao.VehiculeDao;
import service.AffectationService;
import model.AffectationDetails;
import model.Reservation;
import model.Vehicule;
import myframework.annotation.GET;
import myframework.annotation.MyController;
import myframework.annotation.MyMapping;
import myframework.annotation.POST;
import myframework.annotation.RequestParam;
import myframework.fw.ModelView;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@MyController
public class AffectationController {
    
    private final AffectationService affectationService = new AffectationService();
    private final AffectationDao affectationDao = new AffectationDao();
    private final VehiculeDao vehiculeDao = new VehiculeDao();
    private final ReservationDao reservationDao = new ReservationDao();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Page d'accueil - Formulaire de recherche par date
     */
    @MyMapping("/affectation")
    @GET
    public ModelView index() {
        return new ModelView("views/recherche-date.jsp");
    }

    /**
     * Recherche les affectations pour une date donnée
     */
    @MyMapping("/affectation/rechercher")
    @GET
    public ModelView rechercher(
        @RequestParam(value = "date", required = true) String dateStr
    ) {
        try {
            Date date = parseDate(dateStr);
            return buildResultatModelView(date, dateStr, null, null);
        } catch (Exception e) {
            return buildRechercheModelView("Erreur lors de la recherche: " + e.getMessage(), dateStr);
        }
    }

    /**
     * Lance l'affectation automatique des véhicules pour une date
     */
    @MyMapping("/affectation/affecter")
    @GET
    public ModelView affecter(
        @RequestParam(value = "date", required = true) String dateStr
    ) {
        try {
            Date date = parseDate(dateStr);
            
            // Effectuer l'affectation
            affectationService.affecterVehicules(date);
            return buildResultatModelView(date, dateStr, "Affectation effectuée avec succès !", null);
        } catch (Exception e) {
            return buildRechercheModelView("Erreur lors de l'affectation: " + e.getMessage(), dateStr);
        }
    }

    /**
     * Liste CRUD globale des affectations (toutes dates)
     */
    @MyMapping("/affectation/crud")
    @GET
    public ModelView crud() {
        try {
            return buildCrudListModelView(null, null);
        } catch (Exception e) {
            return buildRechercheModelView("Erreur lors de l'ouverture du CRUD: " + e.getMessage(), null);
        }
    }

    /**
     * Page d'ajout d'une affectation
     */
    @MyMapping("/affectation/crud/new")
    @GET
    public ModelView newCrud() {
        try {
            return buildCrudCreateFormModelView(null);
        } catch (Exception e) {
            return buildCrudListModelView(null, "Erreur lors de l'ouverture du formulaire d'ajout: " + e.getMessage());
        }
    }

    /**
     * Page de modification d'une affectation (pré-remplie)
     */
    @MyMapping("/affectation/crud/edit")
    @GET
    public ModelView editCrud(
        @RequestParam(value = "idAffectation", required = true) int idAffectation
    ) {
        try {
            AffectationDetails affectation = affectationDao.findDetailById(idAffectation);
            if (affectation == null) {
                return buildCrudListModelView(null, "Affectation introuvable (ID=" + idAffectation + ").");
            }
            return buildCrudEditFormModelView(affectation, null);
        } catch (Exception e) {
            return buildCrudListModelView(null, "Erreur lors de l'ouverture de la modification: " + e.getMessage());
        }
    }

    /**
     * Crée une affectation manuelle.
     */
    @MyMapping("/affectation/crud/create")
    @POST
    public ModelView createCrud(
        @RequestParam(value = "date", required = false) String dateStr,
        @RequestParam(value = "idVehicule", required = true) int idVehicule,
        @RequestParam(value = "idReservation", required = true) int idReservation,
        @RequestParam(value = "dateHeureDepart", required = true) String dateHeureDepart,
        @RequestParam(value = "dateHeureRetour", required = true) String dateHeureRetour,
        @RequestParam(value = "ordreLivraison", required = true) int ordreLivraison,
        @RequestParam(value = "nombrePassagersAffectes", required = true) int nombrePassagersAffectes
    ) {
        try {
            Timestamp depart = parseDateTimeLocal(dateHeureDepart);
            Timestamp retour = parseDateTimeLocal(dateHeureRetour);

            affectationDao.insert(
                idVehicule,
                idReservation,
                depart,
                retour,
                ordreLivraison,
                nombrePassagersAffectes
            );

            return buildCrudListModelView("Affectation créée avec succès.", null);
        } catch (Exception e) {
            try {
                return buildCrudCreateFormModelView("Erreur lors de la création: " + e.getMessage());
            } catch (Exception ex) {
                return buildCrudListModelView(null, "Erreur lors de la création: " + e.getMessage());
            }
        }
    }

    /**
     * Modifie une affectation existante.
     */
    @MyMapping("/affectation/crud/update")
    @POST
    public ModelView updateCrud(
        @RequestParam(value = "date", required = false) String dateStr,
        @RequestParam(value = "idAffectation", required = true) int idAffectation,
        @RequestParam(value = "idVehicule", required = true) int idVehicule,
        @RequestParam(value = "idReservation", required = true) int idReservation,
        @RequestParam(value = "dateHeureDepart", required = true) String dateHeureDepart,
        @RequestParam(value = "dateHeureRetour", required = true) String dateHeureRetour,
        @RequestParam(value = "ordreLivraison", required = true) int ordreLivraison,
        @RequestParam(value = "nombrePassagersAffectes", required = true) int nombrePassagersAffectes
    ) {
        try {
            Timestamp depart = parseDateTimeLocal(dateHeureDepart);
            Timestamp retour = parseDateTimeLocal(dateHeureRetour);

            affectationDao.update(
                idAffectation,
                idVehicule,
                idReservation,
                depart,
                retour,
                ordreLivraison,
                nombrePassagersAffectes
            );

            return buildCrudListModelView("Affectation modifiée avec succès.", null);
        } catch (Exception e) {
            try {
                AffectationDetails affectation = affectationDao.findDetailById(idAffectation);
                if (affectation != null) {
                    return buildCrudEditFormModelView(affectation, "Erreur lors de la modification: " + e.getMessage());
                }
            } catch (Exception ignored) {
            }
            return buildCrudListModelView(null, "Erreur lors de la modification: " + e.getMessage());
        }
    }

    /**
     * Supprime une affectation.
     */
    @MyMapping("/affectation/crud/delete")
    @POST
    public ModelView deleteCrud(
        @RequestParam(value = "date", required = false) String dateStr,
        @RequestParam(value = "idAffectation", required = true) int idAffectation
    ) {
        try {
            affectationDao.deleteById(idAffectation);
            return buildCrudListModelView("Affectation supprimée avec succès.", null);
        } catch (Exception e) {
            return buildCrudListModelView(null, "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private ModelView buildResultatModelView(Date date, String dateStr, String success, String error) throws Exception {
        ModelView mv = new ModelView("views/resultat-affectation.jsp");

        List<AffectationDetails> affectations = affectationService.getAffectationsByDate(date);
        Map<String, List<AffectationService.VoyageAffectation>> affectationsParVehicule = affectationService.groupByVehiculeEtVoyage(affectations);
        List<Reservation> reservationsNonAffectees = affectationService.getReservationsNonAffectees(date);

        mv.addAttribute("dateRecherche", dateStr);
        mv.addAttribute("affectationsParVehicule", affectationsParVehicule);
        mv.addAttribute("reservationsNonAffectees", reservationsNonAffectees);
        mv.addAttribute("nbAffectations", affectations.size());
        mv.addAttribute("nbNonAffectees", reservationsNonAffectees.size());

        if (success != null && !success.isEmpty()) {
            mv.addAttribute("success", success);
        }
        if (error != null && !error.isEmpty()) {
            mv.addAttribute("error", error);
        }

        return mv;
    }

    private ModelView buildCrudListModelView(String success, String error) {
        ModelView mv = new ModelView("views/affectation-crud.jsp");

        try {
            List<AffectationDetails> affectations = affectationDao.findAllDetails();
            mv.addAttribute("affectations", affectations);
        } catch (Exception e) {
            mv.addAttribute("affectations", List.of());
            mv.addAttribute("error", "Erreur de chargement des affectations: " + e.getMessage());
        }

        if (success != null && !success.isEmpty()) {
            mv.addAttribute("success", success);
        }
        if (error != null && !error.isEmpty()) {
            mv.addAttribute("error", error);
        }

        return mv;
    }

    private ModelView buildCrudCreateFormModelView(String error) throws Exception {
        ModelView mv = new ModelView("views/affectation-crud-form.jsp");
        mv.addAttribute("editMode", false);
        mv.addAttribute("vehicules", vehiculeDao.findAll());
        mv.addAttribute("reservations", reservationDao.findNonAffectees());

        if (error != null && !error.isEmpty()) {
            mv.addAttribute("error", error);
        }
        return mv;
    }

    private ModelView buildCrudEditFormModelView(AffectationDetails affectation, String error) throws Exception {
        ModelView mv = new ModelView("views/affectation-crud-form.jsp");
        mv.addAttribute("editMode", true);
        mv.addAttribute("affectation", affectation);
        mv.addAttribute("vehicules", vehiculeDao.findAll());
        mv.addAttribute("reservations", reservationDao.findAll());

        if (error != null && !error.isEmpty()) {
            mv.addAttribute("error", error);
        }
        return mv;
    }

    private ModelView buildRechercheModelView(String error, String dateStr) {
        ModelView mv = new ModelView("views/recherche-date.jsp");
        mv.addAttribute("error", error);
        if (dateStr != null && !dateStr.isEmpty()) {
            mv.addAttribute("dateRecherche", dateStr);
        }
        return mv;
    }

    private Date parseDate(String dateStr) throws ParseException {
        java.util.Date parsed = dateFormat.parse(dateStr);
        return new Date(parsed.getTime());
    }

    private Timestamp parseDateTimeLocal(String dateTimeStr) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
        return Timestamp.valueOf(dateTime);
    }
}
