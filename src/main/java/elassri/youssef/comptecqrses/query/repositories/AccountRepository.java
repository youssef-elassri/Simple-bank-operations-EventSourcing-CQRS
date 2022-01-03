package elassri.youssef.comptecqrses.query.repositories;

import elassri.youssef.comptecqrses.query.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
