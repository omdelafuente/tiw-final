<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="java.util.List, es.uc3m.tiw.domains.Event, es.uc3m.tiw.domains.Usr, java.time.LocalDateTime, org.apache.commons.codec.binary.StringUtils, org.apache.commons.codec.binary.Base64"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href="/css/w3.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="/css/fonts.css">
<link rel="stylesheet" href="/css/font-awesome-4.7.0/css/font-awesome.min.css">
<title>Resultados de la búsqueda</title>
</head>
<style>
.w3-sidebar a {
	font-family: "Roboto", sans-serif
}

body, h1, h2, h3, h4, h5, h6, .w3-wide {
	font-family: "Montserrat", sans-serif;
}
</style>


<body class="w3-content" style="max-width: 1200px">

	<!-- Sidebar/menu -->
	<jsp:include page="sidebarLogged.jsp" />

	<div class="w3-main" style="margin-left: 250px">

		<!-- Top header -->
		<header class="w3-container w3-xlarge">
		<p class="w3-left">Resultados de la búsqueda</p>
		<form action="search" method="post">
			<p class="w3-right">
				<input type="hidden" name="type" value="simple">
				<input class="w3-border" type="text" name="search" style="padding: 8px; font-size: 15px; float: left"
					placeholder="Buscar..." required>
				<button class="w3-bar-item w3-button w3-hover-grey" type="submit">
					<i class="fa fa-search"></i>
				</button>
			</p>
		</form>
		</header>

		<!-- Si no hay resultados: -->
		<%
			List<Event> events = (List<Event>) request.getAttribute("events");
			List<Usr> users = (List<Usr>) request.getAttribute("users");
		%>

		<%
			if (events.isEmpty() && users.isEmpty()) {
		%>

		<div class="w3-container w3-center">
			<p>No hay resultados que coincidan con esa búsqueda.</p>
		</div>

		<%
			} else {
		%>

		<!-- Se muestran los usuarios encontrados -->
		<%
			if (!users.isEmpty()) {
		%>
		<div class="w3-container">
			<h4>Usuarios</h4>
			<hr class="w3-border">
		</div>

		<div class="w3-container w3-center">

			<%
				for (int i = 0; i < users.size(); i++) {
							Usr user = users.get(i);
			%>
			<div class="w3-row w3-border w3-section">
				<div class="w3-container w3-half">
					&nbsp;
					<p>
						<b>Nombre y apellidos: </b><%=user.getName()%>
						<%=user.getSurname()%></p>
					<p>
						<b>Correo electrónico: </b><%=user.getEmail()%></p>
				</div>
				<div class="w3-container w3-half">
					<form method="post" action="editUser">
						<input type="hidden" name="type" value="userToEdit">
						<input type="hidden" name="email" value="<%=user.getEmail()%>">
						<p>
							<button class="w3-button w3-border" style="width: 50%">Modificar datos</button>
						</p>
					</form>

					<form method="post" action="deleteUser">
						<input type="hidden" name="type" value="userToDelete">
						<input type="hidden" name="email" value="<%=user.getEmail()%>">
						<p>
							<button class="w3-button w3-hover-red w3-border" style="width: 50%">Dar de baja</button>
						</p>
					</form>
				</div>
			</div>
			<%
				}
			%>

		</div>

		<%
			}
		%>

		<!-- Se muestran los eventos encontrados: -->
		<%
			if (!events.isEmpty()) {
		%>
		<div class="w3-container">
			<h4>Eventos</h4>
			<hr class="w3-border">
		</div>

		<div class="w3-container w3-center">

			<%
				for (int i = 0; i < events.size(); i++) {
							LocalDateTime dateTime = events.get(i).getEventDate();
			%>

			<div class="w3-row" style="display: flexbox">
				<br>
				<div class="w3-col" style="width: 365px; height: 205px">
					<a href="event?id=<%=events.get(i).getId()%>"><img
						src="<%StringBuilder sb = new StringBuilder();
						sb.append("data:image/png;base64,");
						sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(events.get(i).getImage(), false)));
						out.print(sb.toString());%>"
						class="w3-hover-shadow" style="max-width: 100%; max-height: 100%"></a>
				</div>
				<div class="w3-col" style="width: 50%">
					<p>
						<b><%=events.get(i).getTitle()%></b>
					</p>
					<p><%=events.get(i).getCategory()%></p>
					<%
						if (events.get(i).getState().equals("Disponible")) {
					%>
					<p class="w3-text-green"><%=events.get(i).getAvailableTickets()%>
						entradas disponibles a
						<%=events.get(i).getPrice()%>
						€
					</p>
					<%
						} else {
					%>
					<p class="w3-text-red"><%=events.get(i).getState()%>
					</p>
					<%
						}
					%>
					<p><%=dateTime.toLocalDate()%>
						a las
						<%=dateTime.toLocalTime()%>h
					</p>
					<p><%=events.get(i).getPlace()%></p>
				</div>
				<div class="w3-col" style="width: 10%">
					<a href="event?id=<%=events.get(i).getId()%>&type=editEvent" style="width: 100%" class="w3-button w3-border">Editar</a>
					<a href="cancelEvent?id=<%=events.get(i).getId()%>&type=eventToCancel" style="width: 100%" class="w3-button w3-border">Cancelar</a>
				</div>

			</div>
			<hr class="w3-border">
			<%
				}
			%>


		</div>
		<%} %>


		<%} %>

	</div>

</body>
</html>