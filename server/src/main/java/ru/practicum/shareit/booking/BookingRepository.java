package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // booker

    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime date, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime date, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // owner

    List<Booking> findByItemOwnerId(Long itemId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime time, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime time, Pageable pageable);


    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

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
    @Query("""
                select b from Booking b
                join fetch b.booker
                where b.item.id = :itemId and b.start < :time
                order by b.start desc
                limit 1
            """)
    Optional<Booking> findTopByItemIdAndStartBeforeOrderByStartDesc(Long itemId, LocalDateTime time);

    @Query("""
                select b from Booking b
                join fetch b.booker
                where b.item.id = :itemId and b.start > :time
                order by b.start asc
                limit 1
            """)
    Optional<Booking> findTopByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime time);

    @Query("""
            select b from Booking b
            join fetch b.item
            join fetch b.booker
            where b.id = :id
            """)
    Optional<Booking> findByIdWithRelations(Long id);

}
