package ru.practicum.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // booker

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // owner

    List<Booking> findByItemOwnerId(Long itemId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime time, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime time, Sort sort);


    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // booleans

    boolean existsByItemIdAndBookerIdAndEndBefore(Long itemId, Long userId, LocalDateTime time);

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

    Optional<Booking> findTopByItemIdAndStartBeforeOrderByStartDesc(Long itemId, LocalDateTime time);

    Optional<Booking> findTopByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime time);

    @Query("""
            select b from Booking b
            join fetch b.item
            join fetch b.booker
            where b.id = :id
            """)
    Optional<Booking> findByIdWithRelations(Long id);

}
