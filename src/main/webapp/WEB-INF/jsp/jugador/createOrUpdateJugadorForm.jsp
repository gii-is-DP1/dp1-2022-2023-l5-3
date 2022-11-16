<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="jugadores">
    <h2>
        <c:if test="${jugador['new']}">Nuevo</c:if> Jugador
    </h2>
    <form:form modelAttribute="jugador" class="form-horizontal" id="add-jugador-form">
        <div class="form-group has-feedback">
            <petclinic:inputField label="First Name" name="firstName"/>
            <petclinic:inputField label="Last Name" name="lastName"/>
            <c:choose>
                    <c:when test="${jugador['new']}">
                        <petclinic:inputField label="Username" name="user.username"/>
                        <petclinic:inputField label="Password" name="user.password"/>
                    </c:when>
                    <c:otherwise>
                        <input type="hidden" name="user.username" value="${username}">
                        <input type="hidden" name="user.password" value="${pass}">
                    </c:otherwise>
                </c:choose>
            
            
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <c:choose>
                    <c:when test="${jugador['new']}">
                        <button class="btn btn-default" type="submit">Registrarse</button>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-default" type="submit">Actualizar datos</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <form:errors></form:errors>
    </form:form>

</petclinic:layout>
