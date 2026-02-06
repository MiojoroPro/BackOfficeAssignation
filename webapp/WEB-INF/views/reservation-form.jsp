<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Hotel" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Nouvelle réservation</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; }
        .container { max-width: 600px; margin: auto; }
        .error { color: #b00020; margin-bottom: 10px; }
        label { display: block; margin-top: 12px; }
        input, select { width: 100%; padding: 8px; margin-top: 4px; }
        button { margin-top: 16px; padding: 10px 16px; }
    </style>
</head>
<body>
<div class="container">
    <h2>Créer une réservation</h2>

    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
        <div class="error"><%= error %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/reservations" method="post">
        <label for="idClient">Id Client (4 caractères alphanumériques)</label>
        <input type="text" id="idClient" name="idClient" maxlength="4" minlength="4" required />

        <label for="nbpassagers">Nombre de passagers</label>
        <input type="number" id="nbpassagers" name="nbpassagers" min="1" required />

        <label for="dateheure">Date et heure</label>
        <input type="datetime-local" id="dateheure" name="dateheure" required />

        <label for="idHotel">Hôtel</label>
        <select id="idHotel" name="idHotel" required>
            <option value="">-- Choisir un hôtel --</option>
            <%
                List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                if (hotels != null) {
                    for (Hotel h : hotels) {
            %>
                <option value="<%= h.getIdHotel() %>"><%= h.getNom() %> - <%= h.getAdresse() %></option>
            <%
                    }
                }
            %>
        </select>

        <button type="submit">Enregistrer</button>
    </form>
</div>
</body>
</html>
