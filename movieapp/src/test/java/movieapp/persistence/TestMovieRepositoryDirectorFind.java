package movieapp.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import movieapp.entity.Artist;
import movieapp.entity.Movie;

@DataJpaTest
class TestMovieRepositoryDirectorFind {

	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	MovieRepository movieRepository;
	
	Movie movieH;
	Movie movieA;
	
	@BeforeEach
	void initData() {
		// artists
		var clint = new Artist("Clint Eastwood", LocalDate.of(1930,5,31));
		var todd = new Artist("Todd Phillips", LocalDate.of(1970,12,20));
		var morgan = new Artist("Morgan Freeman", LocalDate.of(1937,6,1));
		var bradley = new Artist("Bradley Cooper");
		var zach = new Artist("Zach Galifianakis");
		Stream.of(clint, todd, morgan, bradley, zach)
			.forEach(entityManager::persist);
		// movies with director and actors
		var movieUnforgiven = new Movie("Unforgiven", 1992, null);
		var movieGranTorino = new Movie("Gran Torino", 2008, null);
		var movieInvictus = new Movie("Invictus", 2009, null);
		var moviesClint = List.of(movieUnforgiven, movieGranTorino, movieInvictus);
		moviesClint.forEach(m -> m.setDirector(clint));
		movieUnforgiven.setActors(List.of(clint, morgan));
		movieGranTorino.setActors(List.of(clint));
		movieInvictus.setActors(List.of(morgan));
		movieH = new Movie("The Hangover", 2009, null);
		movieH.setDirector(todd);
		movieH.setActors(List.of(bradley, zach));
		movieA = new Movie("Alien", 1979, null);
		moviesClint.forEach(entityManager::persist); // insert x 3
		entityManager.persist(movieH); // insert
		entityManager.persist(movieA); // insert
		entityManager.flush(); // synchro database
		entityManager.clear();
	}

	@Test
	void testFindMovieWithExistingDirector() {
		int idMovie = movieH.getId();
		var optMovie = movieRepository.findById(idMovie);
		assertTrue(optMovie.isPresent());
		// assertNotNull(optMovie.get().getDirector());
		optMovie.ifPresent(m -> assertNotNull(m.getDirector(), "director present"));
	}
	
	@Test
	void testFindMovieWithNoDirector() {
		int idMovie = movieA.getId();
		var optMovie = movieRepository.findById(idMovie);
		assertTrue(optMovie.isPresent());
		// assertNotNull(optMovie.get().getDirector());
		optMovie.ifPresent(m -> assertNull(m.getDirector(), "no director"));
	}
	
	@Test
	void testFindByDirector() {
		// given
		String name = "Clint Eastwood";
		// when 
		var moviesFound  = movieRepository.findByDirectorName(name);
		// assert
		System.out.println(moviesFound);
		// TODO: check found 3 movies all directed by Clint
		assertEquals(3, moviesFound.size(), "number movies");
		assertAll(
			moviesFound.stream()
				.map(Movie::getDirector)
				.map(Artist::getName)
				.map(n -> () -> assertEquals(name, n, "director name")));
	}
	
	@Test
	void testFindMovieWithActors() {
		int idMovie = movieH.getId();
		var movie = movieRepository.getOne(idMovie);
		var actors = movie.getActors();
		System.out.println("Movie: " + movie + " with actors: " + actors);
		assertEquals(2, actors.size());
	}
	
	@Test
	void testFindMovieWithNoActors() {
		int idMovie = movieA.getId();
		var movie = movieRepository.getOne(idMovie);
		var actors = movie.getActors();
		System.out.println("Movie: " + movie + " with actors: " + actors);
		assertEquals(0, actors.size());
	}
	
	@Test
	void testFindByActor() {
		// given
		String name = "Clint Eastwood";
		// when 
		var moviesFound  = movieRepository.findByActorsName(name);
		// assert
		System.out.println(moviesFound);
		// TODO: check found 2 movies all in which Clint plays
		assertEquals(2, moviesFound.size(), "number movies");
//		assertAll(
//			moviesFound.stream()
//				.map(Movie::getActors)
//				.map(Artist::getName)
//				.map(n -> () -> assertEquals(name, n, "director name")));
		for (var m: moviesFound) {
			var actors = m.getActors();
			assertTrue(
					actors.stream()
						.anyMatch(a -> a.getName().equals(name)),
					"at least one actor named clint eastwood");
		}
	}
			
}
