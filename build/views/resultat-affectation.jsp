<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Affectations - ${dateRecherche}</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f8f9fa;
            min-height: 100vh;
            padding: 24px;
            color: #333;
        }
        .container { max-width: 1100px; margin: 0 auto; }
        
        /* Header */
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            flex-wrap: wrap;
            gap: 16px;
        }
        .header h1 {
            font-size: 22px;
            font-weight: 600;
            color: #1a1a1a;
        }
        .header-actions { display: flex; gap: 12px; align-items: center; }
        .badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 500;
        }
        .badge-success { background: #d4edda; color: #155724; }
        .badge-warning { background: #fff3cd; color: #856404; }
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary { background: #0066cc; color: #fff; }
        .btn-primary:hover { background: #0052a3; }
        
        /* Alert */
        .alert {
            padding: 12px 16px;
            border-radius: 6px;
            margin-bottom: 24px;
            font-size: 14px;
        }
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        /* Card */
        .card {
            background: #fff;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            margin-bottom: 24px;
            overflow: hidden;
        }
        .card-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e0e0e0;
            background: #f0f0f0;
        }
        .card-header h2 {
            font-size: 16px;
            font-weight: 600;
            color: #1a1a1a;
        }
        .card-header.warning {
            background: #f0f0f0;
        }
        .card-body { padding: 0; }
        
        /* Vehicule Section */
        .vehicule-section {
            border-bottom: 1px solid #e0e0e0;
        }
        .vehicule-section:last-child { border-bottom: none; }
        .vehicule-header {
            padding: 16px 20px;
            background: #f0f0f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }
        .vehicule-info h3 {
            font-size: 15px;
            font-weight: 600;
            color: #1a1a1a;
            margin-bottom: 4px;
        }
        .vehicule-meta {
            font-size: 13px;
            color: #666;
        }
        .vehicule-meta span {
            margin-right: 12px;
        }
        .tag {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 500;
        }
        .tag-diesel { background: #e8f5e9; color: #2e7d32; }
        .tag-essence { background: #fff3e0; color: #e65100; }
        .trajets-count {
            font-size: 13px;
            color: #666;
        }
        
        /* Table */
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        th {
            text-align: left;
            padding: 12px 20px;
            background: #fff;
            border-bottom: 1px solid #e0e0e0;
            font-weight: 500;
            color: #666;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        td {
            padding: 12px 20px;
            border-bottom: 1px solid #f0f0f0;
            color: #333;
        }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: #fafafa; }
        .text-muted { color: #888; }
        .passagers {
            background: #e3f2fd;
            color: #1565c0;
            padding: 2px 8px;
            border-radius: 4px;
            font-weight: 500;
            font-size: 13px;
        }
        .time {
            font-family: 'SF Mono', Monaco, monospace;
            font-size: 13px;
            color: #555;
        }
        
        /* Empty state */
        .empty-state {
            padding: 48px 20px;
            text-align: center;
            color: #888;
        }
        .empty-state p { margin-bottom: 8px; }
        
        /* Warning card */
        .card-warning .card-header {
            background: #fff3cd;
            border-bottom-color: #ffc107;
        }
        .card-warning .card-header h2 { color: #856404; }
        .row-warning { background: #fffbeb; }
        
        /* Footer actions */
        .footer-actions {
            text-align: center;
            margin-top: 32px;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>Affectations du ${dateRecherche}</h1>
            <div class="header-actions">
                <span class="badge badge-success">${nbAffectations} affectation(s)</span>
                <span class="badge badge-warning">${nbNonAffectees} non affectée(s)</span>
            </div>
        </div>
        
        <c:if test="${not empty success}">
            <div class="alert alert-success">${success}</div>
        </c:if>
        
        <!-- Véhicules et affectations -->
        <div class="card">
            <div class="card-header">
                <h2>Véhicules et réservations assignées</h2>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty affectationsParVehicule}">
                        <div class="empty-state">
                            <p>Aucune affectation pour cette date</p>
                            <p class="text-muted">Cliquez sur "Relancer l'affectation" pour assigner les véhicules</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="entry" items="${affectationsParVehicule}">
                            <div class="vehicule-section">
                                <div class="vehicule-header">
                                    <div class="vehicule-info">
                                        <h3>${entry.key}</h3>
                                        <div class="vehicule-meta">
                                            <span>${entry.value[0].capacite} places</span>
                                            <span class="tag ${entry.value[0].diesel ? 'tag-diesel' : 'tag-essence'}">
                                                ${entry.value[0].carburantLibelle}
                                            </span>
                                        </div>
                                    </div>
                                    <div class="trajets-count">${entry.value.size()} trajet(s)</div>
                                </div>
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Ordre</th>
                                            <th>Client</th>
                                            <th>Passagers</th>
                                            <th>Départ</th>
                                            <th>Heure départ</th>
                                            <th>Destination</th>
                                            <th>Heure retour</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="affectation" items="${entry.value}">
                                            <tr>
                                                <td><span class="badge badge-success">${affectation.ordreLivraison}</span></td>
                                                <td><strong>${affectation.idClient}</strong></td>
                                                <td><span class="passagers">${affectation.nombrePassagers}</span></td>
                                                <td>${affectation.lieuDepart}</td>
                                                <td><span class="time"><fmt:formatDate value="${affectation.dateHeureDepart}" pattern="HH:mm"/></span></td>
                                                <td>${affectation.lieuArrivee}</td>
                                                <td><span class="time"><fmt:formatDate value="${affectation.dateHeureRetour}" pattern="HH:mm"/></span></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <!-- Réservations non affectées -->
        <c:if test="${not empty reservationsNonAffectees}">
            <div class="card card-warning">
                <div class="card-header warning">
                    <h2>Réservations sans véhicule</h2>
                </div>
                <div class="card-body">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Client</th>
                                <th>Passagers</th>
                                <th>Heure</th>
                                <th>Destination</th>
                                <th>Raison</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="reservation" items="${reservationsNonAffectees}">
                                <tr class="row-warning">
                                    <td>#${reservation.id}</td>
                                    <td>${reservation.idClient}</td>
                                    <td><span class="passagers">${reservation.nombrePassagers}</span></td>
                                    <td><span class="time"><fmt:formatDate value="${reservation.dateHeureDepart}" pattern="HH:mm"/></span></td>
                                    <td>${reservation.lieuDestination}</td>
                                    <td class="text-muted">
                                        <c:choose>
                                            <c:when test="${reservation.nombrePassagers > 25}">Capacité max dépassée</c:when>
                                            <c:otherwise>Véhicules indisponibles</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
        
        <!-- Footer -->
        <div class="footer-actions">
            <a href="affecter?date=${dateRecherche}" 
               class="btn btn-primary"
               onclick="return confirm('Relancer l\'affectation automatique ?')">
                Relancer l'affectation
            </a>
        </div>
    </div>
</body>
</html>
