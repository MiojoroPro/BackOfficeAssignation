<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CRUD Affectations</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f8f9fa;
            min-height: 100vh;
            padding: 24px;
            color: #1f2937;
        }
        .container { max-width: 1180px; margin: 0 auto; }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
            margin-bottom: 18px;
        }
        .title h1 { font-size: 22px; margin-bottom: 4px; }
        .title p { color: #6b7280; font-size: 14px; }
        .actions { display: flex; gap: 10px; }
        .btn {
            border: 1px solid #d1d5db;
            background: #fff;
            color: #111827;
            text-decoration: none;
            padding: 8px 12px;
            border-radius: 6px;
            font-size: 14px;
            cursor: pointer;
        }
        .btn-primary {
            background: #0b67c2;
            border-color: #0b67c2;
            color: #fff;
        }
        .btn-danger {
            background: #dc2626;
            border-color: #dc2626;
            color: #fff;
        }
        .alert {
            padding: 10px 14px;
            border-radius: 6px;
            margin-bottom: 16px;
            font-size: 14px;
        }
        .alert-success { background: #dcfce7; color: #14532d; border: 1px solid #86efac; }
        .alert-error { background: #fef2f2; color: #7f1d1d; border: 1px solid #fca5a5; }
        .grid {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 16px;
            margin-bottom: 16px;
        }
        .card {
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            overflow: hidden;
        }
        .card h2 {
            font-size: 16px;
            padding: 12px 14px;
            background: #f3f4f6;
            border-bottom: 1px solid #e5e7eb;
        }
        .card-body { padding: 14px; }
        .span-6 { grid-column: span 6; }
        .span-12 { grid-column: span 12; }
        .form-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 10px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-size: 13px;
            color: #374151;
        }
        .form-group input,
        .form-group select {
            width: 100%;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            padding: 8px;
            font-size: 14px;
        }
        .form-actions {
            margin-top: 10px;
            display: flex;
            gap: 8px;
            align-items: center;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
        }
        th, td {
            border-bottom: 1px solid #e5e7eb;
            padding: 8px;
            text-align: left;
        }
        th { background: #f9fafb; color: #4b5563; }
        .text-muted { color: #6b7280; font-size: 13px; }
        @media (max-width: 900px) {
            .span-6, .span-12 { grid-column: span 12; }
            .form-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="title">
            <h1>CRUD des Affectations</h1>
            <p>Gestion globale des affectations existantes</p>
        </div>
        <div class="actions">
            <a class="btn" href="${pageContext.request.contextPath}/affectation">Retour recherche</a>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <div class="grid">
        <div class="card span-6">
            <h2>Creer une affectation</h2>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/affectation/crud/create">
                    <input type="hidden" name="date" value="${dateRecherche}">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>Vehicule</label>
                            <select name="idVehicule" required>
                                <c:forEach var="v" items="${vehicules}">
                                    <option value="${v.id}">${v.id} - ${v.immatriculation} (${v.capacite} pl)</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Reservation non affectee</label>
                            <select name="idReservation" required>
                                <c:choose>
                                    <c:when test="${empty reservationsNonAffectees}">
                                        <option value="">Aucune reservation non affectee</option>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="r" items="${reservationsNonAffectees}">
                                            <option value="${r.id}">${r.id} - ${r.idClient} (${r.nombrePassagers} passagers)</option>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Date heure depart</label>
                            <input type="datetime-local" name="dateHeureDepart" required>
                        </div>
                        <div class="form-group">
                            <label>Date heure retour</label>
                            <input type="datetime-local" name="dateHeureRetour" required>
                        </div>
                        <div class="form-group">
                            <label>Ordre livraison</label>
                            <input type="number" name="ordreLivraison" min="1" value="1" required>
                        </div>
                        <div class="form-group">
                            <label>Passagers affectes</label>
                            <input type="number" name="nombrePassagersAffectes" min="1" value="1" required>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button class="btn btn-primary" type="submit">Creer</button>
                        <span class="text-muted">Insertion directe dans la table affectation.</span>
                    </div>
                </form>
            </div>
        </div>

        <div class="card span-6">
            <h2>Modifier une affectation</h2>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/affectation/crud/update">
                    <input type="hidden" name="date" value="${dateRecherche}">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>ID affectation</label>
                            <input type="number" name="idAffectation" min="1" required>
                        </div>
                        <div class="form-group">
                            <label>ID vehicule</label>
                            <input type="number" name="idVehicule" min="1" required>
                        </div>
                        <div class="form-group">
                            <label>ID reservation</label>
                            <input type="number" name="idReservation" min="1" required>
                        </div>
                        <div class="form-group">
                            <label>Ordre livraison</label>
                            <input type="number" name="ordreLivraison" min="1" required>
                        </div>
                        <div class="form-group">
                            <label>Passagers affectes</label>
                            <input type="number" name="nombrePassagersAffectes" min="1" required>
                        </div>
                        <div class="form-group">
                            <label>Date heure depart</label>
                            <input type="datetime-local" name="dateHeureDepart" required>
                        </div>
                        <div class="form-group">
                            <label>Date heure retour</label>
                            <input type="datetime-local" name="dateHeureRetour" required>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button class="btn btn-primary" type="submit">Modifier</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card span-12">
            <h2>Supprimer une affectation</h2>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/affectation/crud/delete" style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                    <input type="hidden" name="date" value="${dateRecherche}">
                    <label for="idAffectationDelete">ID affectation</label>
                    <input id="idAffectationDelete" type="number" name="idAffectation" min="1" required style="max-width:180px;">
                    <button class="btn btn-danger" type="submit" onclick="return confirm('Supprimer cette affectation ?')">Supprimer</button>
                </form>
            </div>
        </div>

        <div class="card span-12">
            <h2>Affectations existantes (${affectations.size()})</h2>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty affectations}">
                        <p class="text-muted">Aucune affectation existante.</p>
                    </c:when>
                    <c:otherwise>
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Vehicule</th>
                                    <th>Reservation</th>
                                    <th>Client</th>
                                    <th>Passagers</th>
                                    <th>Depart</th>
                                    <th>Retour</th>
                                    <th>Ordre</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="a" items="${affectations}">
                                    <tr>
                                        <td>${a.idAffectation}</td>
                                        <td>${a.idVehicule} - ${a.immatriculation}</td>
                                        <td>${a.idReservation}</td>
                                        <td>${a.idClient}</td>
                                        <td>${a.nombrePassagers}</td>
                                        <td><fmt:formatDate value="${a.dateHeureDepart}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td><fmt:formatDate value="${a.dateHeureRetour}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td>${a.ordreLivraison}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
</body>
</html>
