package ru.hack.greenmarchrootes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hack.greenmarchrootes.model.Station;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
}
