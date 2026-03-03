<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Lieu" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Nouvelle Réservation</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 600px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #333; }
        label { display: block; margin-top: 15px; font-weight: bold; }
        input, select { width: 100%; padding: 10px; margin-top: 5px; box-sizing: border-box; }
        input[type="submit"] { 
            margin-top: 20px; 
            background-color: #4CAF50; 
            color: white; 
            border: none; 
            cursor: pointer; 
        }
        input[type="submit"]:hover { background-color: #45a049; }
        .error { color: red; margin-bottom: 15px; padding: 10px; background-color: #fee; }
        .info { background: #e7f3fe; padding: 10px; margin-bottom: 20px; border-left: 4px solid #2196F3; }
        .nav { margin-bottom: 20px; }
        .nav a { 
            display: inline-block; 
            padding: 10px 20px; 
            background: #667eea; 
            color: white; 
            text-decoration: none; 
            border-radius: 5px; 
        }
        .nav a:hover { background: #764ba2; }
    </style>
</head>
<body>
    <div class="nav">
        <a href="${pageContext.request.contextPath}/affectation">🚐 Gestion des Affectations</a>
    </div>

    <h1>Nouvelle Réservation</h1>
    
    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
        <div class="error"><%= error %></div>
    <% } %>

    <div class="info">
        L'identifiant client doit comporter exactement <strong>4 caractères</strong> (lettres et/ou chiffres).
    </div>

    <form action="${pageContext.request.contextPath}/reservations" method="post">
        <label for="idClient">Identifiant Client (4 caractères)</label>
        <input type="text" id="idClient" name="idClient" pattern="[A-Za-z0-9]{4}" maxlength="4" required placeholder="Ex: AB12">

        <label for="nbpassagers">Nombre de passagers</label>
        <input type="number" id="nbpassagers" name="nbpassagers" min="1" required>

        <label for="dateheure">Date et heure de départ (depuis l'aéroport)</label>
        <input type="datetime-local" id="dateheure" name="dateheure" required>

        <label for="idLieu">Hôtel de destination</label>
        <select id="idLieu" name="idLieu" required>
            <option value="">-- Sélectionner un hôtel --</option>
            <% 
                List<Lieu> hotels = (List<Lieu>) request.getAttribute("hotels");
                if (hotels != null) {
                    for (Lieu hotel : hotels) { 
            %>
                <option value="<%= hotel.getId() %>"><%= hotel.getCode() %> - <%= hotel.getLibelle() %></option>
            <% 
                    }
                }
            %>
        </select>

        <input type="submit" value="Réserver">
    </form>
</body>
</html>
