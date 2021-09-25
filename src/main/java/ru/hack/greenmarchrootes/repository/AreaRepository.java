package ru.hack.greenmarchrootes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hack.greenmarchrootes.model.Area;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
}
