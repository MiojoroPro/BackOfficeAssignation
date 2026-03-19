<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Affectation des Véhicules</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f8f9fa;
            min-height: 100vh;
            padding: 40px 20px;
        }
        .container {
            max-width: 480px;
            margin: 0 auto;
        }
        .card {
            background: #fff;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 32px;
        }
        h1 {
            font-size: 24px;
            font-weight: 600;
            color: #1a1a1a;
            margin-bottom: 8px;
        }
        .subtitle {
            color: #666;
            font-size: 14px;
            margin-bottom: 32px;
        }
        .form-group {
            margin-bottom: 24px;
        }
        label {
            display: block;
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 8px;
        }
        input[type="date"] {
            width: 100%;
            padding: 12px;
            border: 1px solid #d0d0d0;
            border-radius: 6px;
            font-size: 15px;
            color: #333;
        }
        input[type="date"]:focus {
            outline: none;
            border-color: #0066cc;
            box-shadow: 0 0 0 3px rgba(0, 102, 204, 0.1);
        }
        .btn-group {
            display: flex;
            gap: 12px;
        }
        .btn {
            flex: 1;
            padding: 12px 20px;
            border: none;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-primary {
            background: #0066cc;
            color: #fff;
        }
        .btn-primary:hover { background: #0052a3; }
        .btn-secondary {
            background: #28a745;
            color: #fff;
        }
        .btn-secondary:hover { background: #218838; }
        .alert {
            padding: 12px 16px;
            border-radius: 6px;
            margin-bottom: 24px;
            font-size: 14px;
        }
        .alert-error {
            background: #fef2f2;
            color: #dc2626;
            border: 1px solid #fecaca;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="card">
            <h1>Affectation des Véhicules</h1>
            <p class="subtitle">Sélectionnez une date pour gérer les affectations</p>
            
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>
            
            <form id="searchForm">
                <div class="form-group">
                    <label for="date">Date</label>
                    <input type="date" id="date" name="date" required 
                           value="${dateRecherche != null ? dateRecherche : ''}">
                </div>
                
                <div class="btn-group">
                    <button type="button" class="btn btn-primary" onclick="rechercher()">Rechercher</button>
                    <button type="button" class="btn btn-secondary" onclick="affecter()">Affecter</button>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        function rechercher() {
            var date = document.getElementById('date').value;
            if (date) {
                window.location.href = 'affectation/rechercher?date=' + date;
            } else {
                alert('Veuillez sélectionner une date');
            }
        }
        
        function affecter() {
            var date = document.getElementById('date').value;
            if (date) {
                if (confirm('Lancer l\'affectation automatique pour cette date ?')) {
                    window.location.href = 'affectation/affecter?date=' + date;
                }
            } else {
                alert('Veuillez sélectionner une date');
            }
        }
        
        document.getElementById('date').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') { e.preventDefault(); rechercher(); }
        });
    </script>
</body>
</html>
