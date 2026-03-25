<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:choose><c:when test="${editMode}">Modifier</c:when><c:otherwise>Nouveau</c:otherwise></c:choose> affectation</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f8f9fa;
            min-height: 100vh;
            padding: 24px;
            color: #1f2937;
        }
        .container { max-width: 860px; margin: 0 auto; }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
            gap: 10px;
            flex-wrap: wrap;
        }
        .header h1 { font-size: 22px; }
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
        .card {
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            overflow: hidden;
        }
        .card-body { padding: 16px; }
        .alert {
            padding: 10px 14px;
            border-radius: 6px;
            margin-bottom: 16px;
            font-size: 14px;
            background: #fef2f2;
            color: #7f1d1d;
            border: 1px solid #fca5a5;
        }
        .form-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
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
            margin-top: 14px;
            display: flex;
            gap: 8px;
            align-items: center;
        }
        .text-muted { color: #6b7280; font-size: 13px; }
        @media (max-width: 760px) {
            .form-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>
            <c:choose>
                <c:when test="${editMode}">Modifier une affectation</c:when>
                <c:otherwise>Nouvelle affectation</c:otherwise>
            </c:choose>
        </h1>
        <a class="btn" href="${pageContext.request.contextPath}/affectation/crud">Retour liste</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert">${error}</div>
    </c:if>

    <div class="card">
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}<c:choose><c:when test='${editMode}'>/affectation/crud/update</c:when><c:otherwise>/affectation/crud/create</c:otherwise></c:choose>">
                <c:if test="${editMode}">
                    <input type="hidden" name="idAffectation" value="${affectation.idAffectation}">
                </c:if>

                <div class="form-grid">
                    <div class="form-group">
                        <label>Vehicule</label>
                        <select name="idVehicule" required>
                            <c:forEach var="v" items="${vehicules}">
                                <option value="${v.id}" <c:if test="${editMode and v.id == affectation.idVehicule}">selected</c:if>>
                                    ${v.id} - ${v.immatriculation} (${v.capacite} pl)
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Reservation</label>
                        <select name="idReservation" required>
                            <c:choose>
                                <c:when test="${empty reservations}">
                                    <option value="">Aucune reservation disponible</option>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="r" items="${reservations}">
                                        <option value="${r.id}" <c:if test="${editMode and r.id == affectation.idReservation}">selected</c:if>>
                                            ${r.id} - ${r.idClient} (${r.nombrePassagers} passagers)
                                        </option>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Date heure depart</label>
                        <input type="datetime-local" name="dateHeureDepart" required
                               value="<c:if test='${editMode}'><fmt:formatDate value='${affectation.dateHeureDepart}' pattern="yyyy-MM-dd'T'HH:mm"/></c:if>">
                    </div>

                    <div class="form-group">
                        <label>Date heure retour</label>
                        <input type="datetime-local" name="dateHeureRetour" required
                               value="<c:if test='${editMode}'><fmt:formatDate value='${affectation.dateHeureRetour}' pattern="yyyy-MM-dd'T'HH:mm"/></c:if>">
                    </div>

                    <div class="form-group">
                        <label>Ordre livraison</label>
                        <input type="number" name="ordreLivraison" min="1" required
                               value="<c:choose><c:when test='${editMode}'>${affectation.ordreLivraison}</c:when><c:otherwise>1</c:otherwise></c:choose>">
                    </div>

                    <div class="form-group">
                        <label>Passagers affectes</label>
                        <input type="number" name="nombrePassagersAffectes" min="1" required
                               value="<c:choose><c:when test='${editMode}'>${affectation.nombrePassagers}</c:when><c:otherwise>1</c:otherwise></c:choose>">
                    </div>
                </div>

                <div class="form-actions">
                    <button class="btn btn-primary" type="submit">
                        <c:choose>
                            <c:when test="${editMode}">Enregistrer</c:when>
                            <c:otherwise>Ajouter</c:otherwise>
                        </c:choose>
                    </button>
                    <span class="text-muted">Les changements seront appliqués dans la table affectation.</span>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
