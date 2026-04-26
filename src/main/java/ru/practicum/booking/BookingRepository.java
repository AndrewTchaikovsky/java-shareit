package ru.practicum.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // booker

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);


    // owner

    List<Booking> findByItem_Owner_Id(Long itemId, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, Status status, Sort sort);

    List<Booking> findByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime time, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime time, Sort sort);


    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // booleans

    boolean existsByItem_IdAndBooker_IdAndEndBefore(Long itemId, Long userId, LocalDateTime time);

    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b
            where b.item.id = :itemId
            and b.status = 'APPROVED'
            and (
            (b.start < :end and b.end > :start)
                 )
            """)
    boolean existOverlapping(Long itemId, LocalDateTime start, LocalDateTime end);

    // booking info

    Optional<Booking> findTopByItem_IdAndStartBeforeOrderByStartDesc(Long itemId, LocalDateTime time);

    Optional<Booking> findTopByItem_IdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime time);

    @Query("""
            select b from Booking b
            join fetch b.item
            join fetch b.booker
            where b.id = :id
            """)
    Optional<Booking> findByIdWithRelations(Long id);

}
