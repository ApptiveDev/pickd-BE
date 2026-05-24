package back.pickd.application.repository;

import back.pickd.application.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
        select t
        from Todo t
        left join fetch t.application
    """)
    List<Todo> findAllWithApplication();

    @Query("""
        select t
        from Todo t
        left join fetch t.application
        where t.application.id = :applicationId
    """)
    List<Todo> findByApplicationId(Long applicationId);
}