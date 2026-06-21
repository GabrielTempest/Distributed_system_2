package distsys26.database_writer.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisasterAlertRepository extends JpaRepository<DBAlertEntity, String> {
    // Basic CRUD methods are automatically inherited here
}