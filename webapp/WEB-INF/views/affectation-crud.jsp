<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Affectations - Liste</title>
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
        .actions { display: flex; gap: 10px; flex-wrap: wrap; }
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
        .btn-small {
            font-size: 12px;
            padding: 6px 10px;
        }
        .alert {
            padding: 10px 14px;
            border-radius: 6px;
            margin-bottom: 16px;
            font-size: 14px;
        }
        .alert-success { background: #dcfce7; color: #14532d; border: 1px solid #86efac; }
        .alert-error { background: #fef2f2; color: #7f1d1d; border: 1px solid #fca5a5; }
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
        .actions-cell {
            display: flex;
            gap: 6px;
            align-items: center;
            flex-wrap: wrap;
        }
        .inline-delete { margin: 0; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="title">
            <h1>Affectations</h1>
            <p>Liste des affectations existantes (toutes dates)</p>
        </div>
        <div class="actions">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/affectation/crud/new">+ Nouveau</a>
            <a class="btn" href="${pageContext.request.contextPath}/affectation">Retour recherche</a>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <div class="card">
        <h2>Liste (${affectations.size()})</h2>
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
                                <th>Actions</th>
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
                                    <td>${a.dateHeureDepart}</td>
                                    <td>${a.dateHeureRetour}</td>
                                    <td>${a.ordreLivraison}</td>
                                    <td>
                                        <div class="actions-cell">
                                            <a class="btn btn-small" href="${pageContext.request.contextPath}/affectation/crud/edit?idAffectation=${a.idAffectation}">Modifier</a>
                                            <form class="inline-delete" method="post" action="${pageContext.request.contextPath}/affectation/crud/delete" onsubmit="return confirm('Supprimer cette affectation ?');">
                                                <input type="hidden" name="idAffectation" value="${a.idAffectation}">
                                                <button class="btn btn-danger btn-small" type="submit">Supprimer</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
</body>
</html>
