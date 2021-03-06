<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="es.uc3m.tiw.domains.Usr"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="/css/w3.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="/css/fonts.css">
<title>Eliminar usuario</title>
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

	<jsp:include page="sidebarLogged.jsp" />

	<!-- !PAGE CONTENT! -->
	<div class="w3-main" style="margin-left: 250px">

		<!-- Top header -->
		<header class="w3-container w3-xlarge">
		<p class="w3-left">Eliminar usuario</p>
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

		<div class="w3-container w3-center">

			<%
				Usr user = (Usr) request.getAttribute("user");
			%>
			
			<%
				if ((Boolean) request.getAttribute("deleteError") != null) {
					
			%>

			<div class="w3-panel w3-red w3-card-4">
				<p><%="El usuario aún tiene eventos activos, por lo que no puede ser eliminado. Cancela todos sus eventos para eliminarlo."%></p>
			</div>
			<%
				}
			%>

			<p>¿Estás seguro de que quieres eliminar este usuario?</p>
			<p>El usuario no podrá volver a acceder al portal.</p>
			<form method="post" action="deleteUser" id="deleteForm">
				<input type="hidden" name="email" value="<%=user.getEmail()%>">
			</form>
			<div class="w3-row">
				<button class="w3-button w3-theme w3-grey w3-medium" type="submit" form="deleteForm">Sí</button>
				<a href="javascript:history.back()" class="w3-button w3-theme w3-grey w3-medium">No</a>
			</div>

		</div>

	</div>
</body>
</html>