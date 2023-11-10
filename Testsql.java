package login.java.spring.security;

import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Resource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.CookieValue;

@RestController
public class GreetingController {
  
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

  @PersistenceContext
  private EntityManager entityManager;

	@Resource
	private SessionFactory sessionFactory;

  @GetMapping("/test1")
  public List<Greeting> test1(@RequestParam(value = "name", defaultValue = "World") String name, HttpServletResponse response) {
    // ruleid: hibernate-sqli
    return entityManager.createQuery("from " + name).getResultList();
  }

  @GetMapping("/test2")
  public List<Greeting> test2(@RequestBody String name) {
    // ruleid: hibernate-sqli
    return entityManager.createQuery("from " + name).getResultList();
  }

  @GetMapping("/test3/{name}")
  public List<Greeting> test3(@PathVariable String name) {
    // ruleid: hibernate-sqli
    return sessionFactory.getCurrentSession().createQuery(String.format("from %s", name)).getResultList();
  }

  @GetMapping("/test4")
  public List<Greeting> test4(@RequestHeader("my-name") String name, HttpServletResponse response) {
    Session session = sessionFactory.openSession();
    // ruleid: hibernate-sqli
    return session.createSQLQuery(String.format("select * from TestEntity where id = %s ", name));
  }

  @GetMapping("/test5")
  public List<Greeting> test5(@CookieValue(value = "foo", defaultValue = "bar") String name, HttpServletResponse response) {
    Session session = sessionFactory.openSession();
    Criteria criteria = session.createCriteria(UserEntity.class);
    // ruleid: hibernate-sqli
    criteria.add(Restrictions.sqlRestriction("param1  = ? and param2 = " + name, new String[] {name}, new Type[] {StandardBasicTypes.STRING}));
    return session.createSQLQuery("select * from user");
  }

  public List<Greeting> okTest1(String name) {
    // ok: hibernate-sqli
    return entityManager.createQuery("from " + name).getResultList();
  }

  @GetMapping("/okTest2")
  public List<Greeting> okTest2(@RequestBody String name) {
    // ok: hibernate-sqli
    return entityManager.createSomethingElse("from " + name);
  }

  @GetMapping("/okTest3")
  public List<Greeting> okTest3(@RequestHeader("my-name") String name, HttpServletResponse response) {
    // ok: hibernate-sqli
    Session session = sessionFactory.openSession();
    String foobar = "Foobar";
    return session.createSQLQuery(String.format("select * from TestEntity where id = %s ", foobar));
  }

  @GetMapping("/okTest4")
  public List<Greeting> okTest4(@RequestHeader("my-name") Boolean name, HttpServletResponse response) {
    Session session = sessionFactory.openSession();
    // ok: hibernate-sqli
    return session.createSQLQuery(String.format("select * from TestEntity where id = %s ", name));
  }

  @GetMapping("/okTest5")
  public List<Greeting> okTest5(@RequestHeader("my-name") String name, HttpServletResponse response) {
    Session session = sessionFactory.openSession();
    // ok: hibernate-sqli
    return session.createSQLQuery(String.format("select * from TestEntity where id = %s ", (name != null)));
  }
}
