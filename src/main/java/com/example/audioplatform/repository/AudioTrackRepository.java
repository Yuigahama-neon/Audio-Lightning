package com.example.audioplatform.repository;

import com.example.audioplatform.entity.AudioTrack;
import com.example.audioplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AudioTrackRepository extends JpaRepository<AudioTrack, Long> {

    long countByOwner(User owner);

    List<AudioTrack> findAllByOrderByUploadedAtDesc();

    @Query("""
            select a from AudioTrack a
            where a.owner = :owner
              and (:query is null
                or lower(a.title) like lower(concat('%', :query, '%'))
                or lower(a.artist) like lower(concat('%', :query, '%')))
              and (:genreId is null or a.genre.id = :genreId)
            order by a.uploadedAt desc
            """)
    List<AudioTrack> searchOwned(@Param("owner") User owner,
                                 @Param("query") String query,
                                 @Param("genreId") Long genreId);
}
