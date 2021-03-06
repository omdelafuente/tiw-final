package es.uc3m.tiw.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.uc3m.tiw.domains.*;

@Controller
public class PageController {
	
	//RestTemplate para consumir los microservicios
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	HttpSession session;
	
	//para evitar que se muestren paginas de error al recibir error de los microservicios
	@Autowired
	public PageController(RestTemplate rt){
		restTemplate = rt;
		restTemplate.setErrorHandler(new ResponseErrorHandler(){

			@Override
			public void handleError(ClientHttpResponse arg0) throws IOException {
				
				
			}

			@Override
			public boolean hasError(ClientHttpResponse arg0) throws IOException {
				
				return false;
			}
			
		});
	}
	
	//pagina de inicio
	@RequestMapping("/")
	public String indexView(Model model){
		
		return "index";
	}

	//registro de un usuario y validacion del formulario de registro
	@RequestMapping("/register")
	public String register(Model model, @RequestParam Map<String, String> params){
		
		Usr user = new Usr();
		
		String url = "http://localhost:11502/user";
		String email = params.get("email");
		String name = params.get("name");
		String surname = params.get("surname");
		String password = params.get("psw");
		String checkPassword = params.get("checkpsw");
		
		user.setIsActive(true);
	    user.setEmail(email);
	    user.setName(name);
	    user.setSurname(surname);
	    user.setPassword(password);
		
		ArrayList<String> errorRegister = new ArrayList<String>();
		boolean registerSuccess = true;
		
		if(!password.equals(checkPassword)){
			
			registerSuccess = false;
			errorRegister.add("La contraseña y la confirmación deben coincidir.");	
		}
		if(password.length() < 6){
			
			registerSuccess = false;
			errorRegister.add("La contraseña debe tener mínimo 6 números o letras.");
		}
		
		if(!name.matches("^[\\p{Space}\\p{L}]+$") || !surname.matches("^[\\p{Space}\\p{L}]+$")){
			
			registerSuccess = false;
			errorRegister.add("Los nombres y apellidos solo pueden contener letras.");
		}
		
		if(!registerSuccess){	
			model.addAttribute("errorRegister", errorRegister);
			model.addAttribute("registerSuccess", registerSuccess);
			return "register.jsp";
		}
		else {
			
			ResponseEntity<Usr> result = restTemplate.postForEntity(url, user, Usr.class);
			if(result.getStatusCode() == HttpStatus.OK){
				model.addAttribute("registerSuccess", true);
				return "login.jsp";
			}
			else{				
				errorRegister.add("Ya existe una cuenta con esa dirección de correo, por favor use otra.");
				model.addAttribute("errorRegister", errorRegister);
				model.addAttribute("registerSuccess", false);
				return "register.jsp";
			}
		}
	}
	
	//inicio de sesion
	@RequestMapping("/login")
	public String login(Model model, @RequestParam Map<String, String> params){
	
		Usr user = new Usr();
		String url = "http://localhost:11502/userCredential";
		
		user.setEmail(params.get("email"));
		user.setPassword(params.get("psw"));
		
		ResponseEntity<Usr> result = restTemplate.postForEntity(url, user, Usr.class);
		
		if(result.getStatusCode() == HttpStatus.OK){
			model.addAttribute("loginSuccess", true);
			session.setAttribute("loggedUser", result.getBody());
			return "index";
		}
		else{

			String errorLogin = null;
			
			if(result.getStatusCode() == HttpStatus.GONE){
				errorLogin = "La cuenta especificada ha sido eliminada.";
			}	
			else if(result.getStatusCode() == HttpStatus.BAD_REQUEST){
				errorLogin = "La contraseña introducida es incorrecta.";
			}			
			else if(result.getStatusCode() == HttpStatus.NOT_FOUND){
				errorLogin = "No se encontró ninguna cuenta con ese e-mail, por favor regístrate si no lo has hecho o introduce una cuenta existente.";
			}
			
			model.addAttribute("errorLogin", errorLogin);
			model.addAttribute("loginSuccess", false);
			return "login.jsp";
			
		}
	}
	
	//modifica los datos del usuario
	@RequestMapping("/edit")
	public String editProfile(Model model, @RequestParam Map<String, String> params){
		
		String name = params.get("name");
		String surname = params.get("surname");
		String oldPassword = params.get("psw");
		String newPassword = params.get("npsw");
		String checkNewPassword = params.get("checknpsw");
		
		ArrayList<String> errorEdit = new ArrayList<String>();
		boolean editSuccess = true;
		
		Usr user = (Usr) session.getAttribute("loggedUser");
		Usr userToUpdate = new Usr();
		userToUpdate.setEmail(user.getEmail());
		userToUpdate.setIsActive(true);
		
		if(!name.isEmpty()){
			
			if(!name.equals(user.getName())){
				if(!name.matches("^[\\p{Space}\\p{L}]+$")){
					editSuccess = false;
					errorEdit.add("El nombre solo puede contener letras.");
				}
				userToUpdate.setName(name);
			}else{
				userToUpdate.setName(user.getName());
			}
			
		}else{
			userToUpdate.setName(user.getName());
		}
		
		if(!surname.isEmpty()){
			if(!surname.equals(user.getSurname())){
				if(!surname.matches("^[\\p{Space}\\p{L}]+$")){
					editSuccess = false;
					errorEdit.add("Los apellidos solo pueden contener letras.");
				}
				userToUpdate.setSurname(surname);
			}else{
				userToUpdate.setSurname(user.getSurname());
			}
		}else{
			userToUpdate.setSurname(user.getSurname());
		}
		
		if(!newPassword.isEmpty()) {
			
			if(checkNewPassword.isEmpty()){
				
				editSuccess = false;
				errorEdit.add("Debes confirmar la nueva contraseña.");
			}else {
				
				if(!newPassword.equals(checkNewPassword)){
					editSuccess = false;
					errorEdit.add("La nueva contraseña y su confirmación deben ser iguales.");
				}
				if(newPassword.equals(user.getPassword())){
					editSuccess = false;
					errorEdit.add("La nueva contraseña debe ser distinta a la antigua.");
				}
				if(newPassword.length() < 6){
					
					editSuccess = false;
					errorEdit.add("La contraseña debe tener mínimo 6 números o letras.");
					
				}
				userToUpdate.setPassword(newPassword);
			}
		}else{
			userToUpdate.setPassword(user.getPassword());
		}
		
		if(newPassword.isEmpty() && (name.isEmpty() || name.equals(user.getName())) && (surname.isEmpty() || surname.equals(user.getSurname()))){
			editSuccess = false;
			errorEdit.add("No hay ningún campo que modificar.");
		}
		
		if(oldPassword.isEmpty()){
			
			editSuccess = false;
			errorEdit.add("Debes introducir la contraseña actual para poder realizar cambios.");
			
		}else{
			
			if(!oldPassword.equals(user.getPassword())){
				
				editSuccess = false;
				errorEdit.add("La contraseña actual no es correcta.");
				
			}
			
		}
		
		model.addAttribute("editSuccess", editSuccess);
		
		if(editSuccess){
			
			String url = "http://localhost:11502/user/{email}";
			
			restTemplate.put(url, userToUpdate, user.getEmail());
			session.setAttribute("loggedUser", userToUpdate);
			return "index";
		}
		else {
			
			model.addAttribute("errorEdit", errorEdit);
			return "editProfile.jsp";
		}
	}
	
	//cierre de sesion
	@RequestMapping("/logOut")
	public String logOut(){
		
		session.invalidate();
		return "index";
		
	}
	
	//dar de baja a un usuario
	@RequestMapping("/dropOut")
	public String dropOut(Model model){
		
		Usr user = (Usr) session.getAttribute("loggedUser");
		
		String url = "http://localhost:11502/user/{email}";
	
		ResponseEntity<Usr> result = restTemplate.exchange(url, HttpMethod.DELETE, null, Usr.class, user.getEmail());
		
		if(result.getStatusCode() == HttpStatus.OK){
			model.addAttribute("dropOutSuccess", true);
			session.invalidate();
			return "index";
		}
		else {
			model.addAttribute("dropOutSuccess", false);
			return "editProfile.jsp";	
		}
	}
	
	//muestra un serie aleatoria de eventos en la pagina principal
	@RequestMapping("/index")
	public String indexPage(Model model){
		
		String url = "http://localhost:11503/event";
		
		ResponseEntity<List<Event>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Event>>() {});
		
		List<Event> events = response.getBody();
		
		List<Event> eventsToShow = new ArrayList<Event>();
		List<Integer> randomNums = null;
		
		if(events.size() > 0){
			randomNums = ThreadLocalRandom.current().ints(0,events.size()).distinct().limit(9).boxed().collect(Collectors.toList());
			
			for(int i = 0; i < randomNums.size(); i++){			
				eventsToShow.add(events.get(randomNums.get(i)));		
			}
		}
		
		model.addAttribute("events", eventsToShow);
		return "index.jsp";
	}
	
	//creacion de un evento
	@RequestMapping("/createEvent")
	public String createEvent(@RequestParam Map<String, String> params, @RequestParam("image") MultipartFile filePart){
		
		String title = params.get("title");
		String category = params.get("category");
		byte[] image = new byte[(int) filePart.getSize()];
	    try {
			filePart.getInputStream().read(image, 0, image.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    BigDecimal price = new BigDecimal(params.get("price"));
	    String inputDate = params.get("date"); 
	    LocalDateTime date = null;
	    try {
	    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	    	date = LocalDateTime.parse(inputDate, formatter);
	    }
	    catch(DateTimeParseException exc){
	    	System.out.println(exc.getMessage());
	    }
	    String place = params.get("place");
	    String description = params.get("description");
	    int availableTickets = Integer.parseInt(params.get("availableTickets"));
		
	    Usr creator = (Usr)session.getAttribute("loggedUser");
	    
	    Event newEvent = new Event();
		newEvent.setTitle(title);
		newEvent.setCategory(category);
		newEvent.setImage(image);
		newEvent.setPrice(price);
		newEvent.setEventDate(date);
		newEvent.setPlace(place);
		newEvent.setDescription(description);
		newEvent.setAvailableTickets(availableTickets);
		newEvent.setState("Disponible");
		newEvent.setCreator(creator);
		
		String url = "http://localhost:11503/event";
		
		restTemplate.postForObject(url, newEvent, Event.class);
		
		return "myCreatedEvents";
	}
	
	//devuelve el evento necesario para visualizar/editar
	@RequestMapping("/event")
	public String getEvent(@RequestParam(value="id", required=true) int id, @RequestParam(value="type", required=false) String type, Model model){
		
		String url = "http://localhost:11503/event/{id}";
		
		Event event = restTemplate.getForObject(url, Event.class, id);
		model.addAttribute("event", event);
		
		if(type != null){				
			return "editEvent.jsp";
		} else {
			return "event.jsp";
		}
	}
	
	//devuelve los eventos creados por el usuario logueado
	@RequestMapping("/myCreatedEvents")
	public String getCreatedEvents(Model model){
		
		Usr user = (Usr)session.getAttribute("loggedUser");
		
		String url = "http://localhost:11503/event?email={email}";
		
		ResponseEntity<List<Event>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Event>>() {}, user.getEmail());
		
		model.addAttribute("events", response.getBody());
		
		return "myCreatedEvents.jsp";
	}
	
	//realiza tanto la búsqueda avanzada como la simple y devuelve los resultados
	@RequestMapping("/search")
	public String search(Model model, @RequestParam(value="type") String type, @RequestParam(value="search", required=false) String str, @RequestParam Map<String, String> params){
		
		List<Event> retrievedEvents = null;
		
		if(type.equals("advanced")){
			
			
			String title = params.get("title");
			String category = params.get("category");
			String place = params.get("place");
			String description = params.get("description");
			String state = params.get("state");
			BigDecimal priceMin = null;
			BigDecimal priceMax = null;
			if(!params.get("priceMin").isEmpty()){
				priceMin = new BigDecimal(params.get("priceMin"));
			}
			if(!params.get("priceMax").isEmpty()){
				priceMax = new BigDecimal(params.get("priceMax"));
			}
			LocalDateTime dateMin = null;
			LocalDateTime dateMax = null;
			
			try {
		    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		    	if(!params.get("dateMin").isEmpty()){
		    		LocalDate dateMinAux =  LocalDate.parse(params.get("dateMin"), formatter);;
		    		dateMin = dateMinAux.atStartOfDay();
		    	}
		    	if(!params.get("dateMax").isEmpty()){
		    		LocalDate dateMaxAux = LocalDate.parse(params.get("dateMax"), formatter);
		    		dateMax = dateMaxAux.atStartOfDay();
		    	}
		    }
			
		    catch(DateTimeParseException exc){
		    	System.out.println(exc.getMessage());
		    }
			
			String url = "http://localhost:11503/event?title={title}&category={category}&place={place}&description={description}&state={state}&priceMin={priceMin}&priceMax={priceMax}&dateMin={dateMin}&dateMax={dateMax}";
			ResponseEntity<List<Event>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Event>>() {}, title, category, place, description, state, priceMin, priceMax, dateMin, dateMax);
			
			retrievedEvents = response.getBody();
			
		}
		else if(type.equals("simple")){
			
			String url = "http://localhost:11503/event?str={str}";
			ResponseEntity<List<Event>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Event>>() {}, str);
			retrievedEvents = response.getBody();
		}
		
		model.addAttribute("events", retrievedEvents);
		return "searchResults.jsp";
	}
	
	//modifica el estado de un evento a cancelado
	@RequestMapping("/cancelEvent")
	public String cancelEvent(@RequestParam int id){
		
		String url = "http://localhost:11503/event/{id}";
		
		Event event = restTemplate.getForObject(url, Event.class, id);
		event.setState("Cancelado");
		
		restTemplate.put(url, event, event.getId());
		
		return "myCreatedEvents";
	}
	
	//edición de los datos de un evento
	@RequestMapping("/editEvent")
	public String editEvent(@RequestParam Map<String, String> params, @RequestParam("image") MultipartFile filePart){
		
		int id = Integer.parseInt(params.get("id"));
		String title = params.get("title");
		String place = params.get("place");
	    String description = params.get("description");
	    BigDecimal price = null;
	    if(!params.get("price").isEmpty()){
			price = new BigDecimal(params.get("price"));
		}
	    int availableTickets = 0;
	    
	    if(!params.get("availableTickets").isEmpty()){
	    	availableTickets = Integer.parseInt(params.get("availableTickets"));
	    }
	    
		String inputDate = params.get("date"); 
	    LocalDateTime date = null;
	    try {
	    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	    	if(!params.get("date").isEmpty()){
	    		date = LocalDateTime.parse(inputDate, formatter);
	    	}    	
	    }
	    catch(DateTimeParseException exc){
	    	System.out.println(exc.getMessage());
	    }
	    
	    String url = "http://localhost:11503/event/{id}";
		Event event = restTemplate.getForObject(url, Event.class, id);
		
		if(filePart.getSize() > 0){
			byte[] image = new byte[(int) filePart.getSize()];
		    try {
				filePart.getInputStream().read(image, 0, image.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		    event.setImage(image);
		}
		
		if(!title.isEmpty() && !title.equals(event.getTitle())){
			event.setTitle(title);
		}
		
		if(!place.isEmpty() && !place.equals(event.getPlace())){
			event.setPlace(place);
		}
		
		if(price != null && !price.equals(event.getPrice())){
			event.setPrice(price);
		}

		if(!description.isEmpty() && !description.equals(event.getDescription())){
			event.setDescription(description);
		}
		
		if(date != null && !date.equals(event.getEventDate())){
			event.setEventDate(date);
		}
		
		if(availableTickets != 0 && availableTickets != event.getAvailableTickets()){
			event.setAvailableTickets(availableTickets);
		}
		
		url = "http://localhost:11503/event/{id}";
		
		restTemplate.put(url, event, event.getId());    
		
		return "event?id="+id;
	}
	
	//chat entre usuario y administrador
	@RequestMapping("/chat")
	public String chatWithAdministrator (Model model, @RequestParam(value="type", required=false) String type, @RequestParam(required=false) String msg){
		
		Usr user = (Usr)session.getAttribute("loggedUser");
		
		if(type == null){
			
			String url = "http://localhost:11503/event?email={email}";
			
			ResponseEntity<List<Event>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Event>>() {}, user.getEmail());
			
			if(response.getBody().isEmpty()){
				model.addAttribute("creator", false);
			}
			else {
				
				url = "http://localhost:11504/chat?receiverEmail={email}&senderEmail=admin@admin.com";
				
				ResponseEntity<List<Message>> messages = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Message>>() {}, user.getEmail());
				
				model.addAttribute("messages", messages.getBody());			
			}
			
		}
		else {
			
			if(type.equals("write")){
				
				Usr admin = new Usr();
				admin.setEmail("admin@admin.com");
				
				Message message = new Message();
				message.setMessage(msg);
				message.setSender(user);
				message.setReceiver(admin);
				
				String url = "http://localhost:11504/chat";
				
				ResponseEntity<Message> result = restTemplate.postForEntity(url, message, Message.class);
				
				if(result.getStatusCode() == HttpStatus.OK){
					model.addAttribute("sendSuccess", true);
				}
				
			}
			else{
				String url = "http://localhost:11504/chat?receiverEmail={email}&senderEmail=admin@admin.com";
				
				ResponseEntity<List<Message>> messages = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Message>>() {}, user.getEmail());
				
				model.addAttribute("messages", messages.getBody());
			}
			
		}
		
		return "chat.jsp";
	}
	
	//compra de entradas para un evento
	@RequestMapping("/buyTicket")
	public String buyTicket( Model model, @RequestParam(required=false)Map<String, String> params, @RequestParam int id, @RequestParam(required=false) String buy, @RequestParam(required=false) Integer tickets){
		
		String url = "http://localhost:11503/event/{id}";
		
		Event event = restTemplate.getForObject(url, Event.class, id);
		Usr client = (Usr)session.getAttribute("loggedUser");
		
		if(buy == null){
			
			model.addAttribute("event", event);
			if(event.getCreator().getEmail().equals(client.getEmail())){
				model.addAttribute("cannotBuy", true);
				return "event.jsp";
			}
			else {
				return "buyTicket.jsp";
			}
		}
		else {
			
			int newTickets = event.getAvailableTickets() - tickets;
			if(newTickets < 0){
				model.addAttribute("purchaseSuccess", false);
				model.addAttribute("event", event);
				return "buyTicket.jsp";
			}
			else {
				
				Transaction transaction = new Transaction();
				transaction.setAmount(event.getPrice().multiply(new BigDecimal(tickets)));
				transaction.setUnits(tickets);
				transaction.setCreditCardCVC(params.get("cvc"));
				transaction.setCreditCardNumber(params.get("cc"));

				try {
				   	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				   	transaction.setCreditCardDate(LocalDate.parse(params.get("date"), formatter));
				}
				catch(DateTimeParseException exc){
				    System.out.println(exc.getMessage());
				}
				
				url= "http://localhost:11505/transaction";
				
				ResponseEntity<String> transactionResult = restTemplate.postForEntity(url, transaction, String.class);
				
				if(transactionResult.getStatusCode() == HttpStatus.PAYMENT_REQUIRED){
					
					model.addAttribute("purchaseSuccess", false);
					model.addAttribute("transactionFailed", transactionResult.getBody());
					model.addAttribute("event", event);
					return "buyTicket.jsp";
					
				}
				else{		
				
					Purchase purchase = new Purchase();
					purchase.setTickets(tickets);
					purchase.setClient(client);
					purchase.setEvent(event);
					
					url = "http://localhost:11503/purchase";
					
					restTemplate.postForEntity(url, purchase, Purchase.class);
					model.addAttribute("purchaseSuccess", true);
					return "index";
				}
				
			}

		}
	}
	
	//devuelve la lista de entradas que se han comprado
	@RequestMapping("/purchasedTickets")
	public String getPurchasedtickets(Model model){
		
		Usr user = (Usr)session.getAttribute("loggedUser");
		
		String url = "http://localhost:11503/purchase?email={email}";
		
		ResponseEntity<List<Purchase>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Purchase>>() {}, user.getEmail());
		
		model.addAttribute("purchases", response.getBody());
		return "purchasedTickets.jsp";
		
	}
	
	//devuelve la lista de entradas que se han vendido para un evento
	@RequestMapping("/soldTickets")
	public String getSoldTickets(Model model, @RequestParam int id){
		
		String url = "http://localhost:11503/purchase?eventId={id}";
		
		ResponseEntity<List<Purchase>> response = restTemplate.exchange(url, HttpMethod.GET, null,	new ParameterizedTypeReference<List<Purchase>>() {}, id);
		
		model.addAttribute("purchases", response.getBody());
		return "soldTickets.jsp";
	}
	
}
