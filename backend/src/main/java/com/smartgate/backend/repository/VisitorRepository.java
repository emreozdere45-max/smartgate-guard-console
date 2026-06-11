    package com.smartgate.backend.repository;

    import com.smartgate.backend.entity.Visitor;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface VisitorRepository extends JpaRepository<Visitor, Long> {
        List<Visitor> findTop50ByOrderByEntryTimeDesc();

        List<Visitor> findByStatusOrderByEntryTimeDesc(String status);
    }