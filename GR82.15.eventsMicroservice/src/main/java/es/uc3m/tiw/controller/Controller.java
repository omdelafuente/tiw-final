package es.uc3m.tiw.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uc3m.tiw.domains.*;

@RestController
@CrossOrigin
public class Controller {
	
	@Autowired
	EventDAO eventDAO;
	
	@RequestMapping(value="/event", method=RequestMethod.POST)
	public Event createEvent(@RequestBody Event event){
		return eventDAO.save(event);
	}
	
	@RequestMapping(value="/event/{id}", method=RequestMethod.GET)
	public Event getEvent(@PathVariable int id){
		return eventDAO.findById(id);
	}
	
	@RequestMapping(value="/event", method=RequestMethod.GET)
	public List<Event> getEvents(
			@RequestParam(value = "str", required = false) String str,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
			@RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
			@RequestParam(value = "dateMin", required = false) LocalDateTime dateMin,
			@RequestParam(value = "dateMax", required = false) LocalDateTime dateMax){
		
		if(str == null && 
		email == null && 
		title == null && 
		category == null && 
		place == null && 
		description == null && 
		state == null && 
		priceMin == null && 
		priceMax == null && 
		dateMin == null && 
		dateMin == null){
			return eventDAO.findAll();
		}
		else {
			
			if(str != null){
				return eventDAO.findMatchingString(str);
			}
			else if(email != null){
				return eventDAO.findByCreatorEmail(email);				
			}
			else {
				return eventDAO.findByMultipleFields(title, category, place, description, state, priceMin, priceMax, dateMin, dateMax);
			}
			
		}
		 
	}
	
}
