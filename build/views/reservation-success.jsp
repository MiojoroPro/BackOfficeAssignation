<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Réservation confirmée</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 600px; margin: 40px auto; padding: 0 20px; text-align: center; }
        .success { 
            background: #dff0d8; 
            color: #3c763d; 
            padding: 20px; 
            border-radius: 5px; 
            margin-bottom: 20px; 
        }
        a { 
            display: inline-block; 
            margin-top: 20px; 
            padding: 10px 20px; 
            background: #4CAF50; 
            color: white; 
            text-decoration: none; 
            border-radius: 5px; 
        }
        a:hover { background: #45a049; }
    </style>
</head>
<body>
    <div class="success">
        <h1>✅ Succès</h1>
        <p><%= request.getAttribute("message") %></p>
    </div>
    <a href="reservations/new">Nouvelle réservation</a>
</body>
</html>
