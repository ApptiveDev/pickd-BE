package back.pickd.application.repository;

import back.pickd.application.entity.Todo;
import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
        select t from Todo t
        left join fetch t.application
        where t.user = :user
        order by t.id desc
    """)
    List<Todo> findAllByUserWithApplication(@Param("user") User user);

    @Query("""
        select t from Todo t
        left join fetch t.application
        where t.application.id = :applicationId
        and t.user = :user
    """)
    List<Todo> findByApplicationIdAndUser(@Param("applicationId") Long applicationId,
                                          @Param("user") User user);

    Optional<Todo> findByIdAndUser(Long id, User user);
}
