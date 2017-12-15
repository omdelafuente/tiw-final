package es.uc3m.tiw.domains;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UsrDAO extends CrudRepository<Usr, String>{

	public Usr findByEmail(String email);
	public List<Usr> findByIsActiveTrue();
	
	@Query("SELECT c FROM Usr c WHERE c.isActive = TRUE AND c.events IS NOT EMPTY")
	public List<Usr> findCreators();
	
	@Query("SELECT c FROM Usr c WHERE c.name LIKE CONCAT('%',:search,'%') OR c.surname LIKE CONCAT('%',:search,'%')")
	public List<Usr> findMatchingString(@Param("search") String search);
} 
