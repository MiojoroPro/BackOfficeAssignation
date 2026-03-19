package controller;

import service.AffectationService;
import model.AffectationDetails;
import model.Reservation;
import myframework.annotation.GET;
import myframework.annotation.MyController;
import myframework.annotation.MyMapping;
import myframework.annotation.RequestParam;
import myframework.fw.ModelView;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@MyController
public class AffectationController {
    
    private final AffectationService affectationService = new AffectationService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
        ModelView mv = new ModelView("views/resultat-affectation.jsp");
        
        try {
            Date date = parseDate(dateStr);
            
            // Récupérer les affectations existantes
            List<AffectationDetails> affectations = affectationService.getAffectationsByDate(date);
            Map<String, List<AffectationService.VoyageAffectation>> affectationsParVehicule = affectationService.groupByVehiculeEtVoyage(affectations);
            
            // Récupérer les réservations non affectées
            List<Reservation> reservationsNonAffectees = affectationService.getReservationsNonAffectees(date);
            
            mv.addAttribute("dateRecherche", dateStr);
            mv.addAttribute("affectationsParVehicule", affectationsParVehicule);
            mv.addAttribute("reservationsNonAffectees", reservationsNonAffectees);
            mv.addAttribute("nbAffectations", affectations.size());
            mv.addAttribute("nbNonAffectees", reservationsNonAffectees.size());
            
        } catch (Exception e) {
            mv = new ModelView("views/recherche-date.jsp");
            mv.addAttribute("error", "Erreur lors de la recherche: " + e.getMessage());
        }
        
        return mv;
    }

    /**
     * Lance l'affectation automatique des véhicules pour une date
     */
    @MyMapping("/affectation/affecter")
    @GET
    public ModelView affecter(
        @RequestParam(value = "date", required = true) String dateStr
    ) {
        ModelView mv = new ModelView("views/resultat-affectation.jsp");
        
        try {
            Date date = parseDate(dateStr);
            
            // Effectuer l'affectation
            AffectationService.ResultatAffectation resultat = affectationService.affecterVehicules(date);
            
            // Récupérer les affectations après traitement
            List<AffectationDetails> affectations = affectationService.getAffectationsByDate(date);
            Map<String, List<AffectationService.VoyageAffectation>> affectationsParVehicule = affectationService.groupByVehiculeEtVoyage(affectations);
            
            // Réservations non affectées
            List<Reservation> reservationsNonAffectees = resultat.getReservationsNonAffectees();
            
            mv.addAttribute("dateRecherche", dateStr);
            mv.addAttribute("affectationsParVehicule", affectationsParVehicule);
            mv.addAttribute("reservationsNonAffectees", reservationsNonAffectees);
            mv.addAttribute("nbAffectations", affectations.size());
            mv.addAttribute("nbNonAffectees", reservationsNonAffectees.size());
            mv.addAttribute("success", "Affectation effectuée avec succès !");
            
        } catch (Exception e) {
            mv = new ModelView("views/recherche-date.jsp");
            mv.addAttribute("error", "Erreur lors de l'affectation: " + e.getMessage());
            mv.addAttribute("dateRecherche", dateStr);
        }
        
        return mv;
    }

    private Date parseDate(String dateStr) throws ParseException {
        java.util.Date parsed = dateFormat.parse(dateStr);
        return new Date(parsed.getTime());
    }
}
