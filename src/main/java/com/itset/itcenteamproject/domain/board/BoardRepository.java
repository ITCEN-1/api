package com.itset.itcenteamproject.domain.board;

import com.itset.itcenteamproject.domain.board.dto.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("""
                select new com.itset.itcenteamproject.domain.board.dto.BoardListItemDTO(
                  b.id, b.title, u.nickname, d.districtName, d.dongName, b.createdAt, b.viewCount
                )
                from Board b join b.user u join DongLocation d on d.dongCode = b.dongCode
                where (:titleKeyword is null or :titleKeyword='' or b.title like concat('%', :titleKeyword, '%'))
                  and (:districtName is null or :districtName='' or d.districtName=:districtName)
                  and (:dongCode is null or b.dongCode=:dongCode)
                order by b.createdAt desc
            """)
    Page<BoardListItemDTO> searchPosts(@Param("titleKeyword") String titleKeyword,
                                       @Param("districtName") String districtName,
                                       @Param("dongCode") Integer dongCode,
                                       Pageable pageable);

    @Query("""
                select new com.itset.itcenteamproject.domain.board.dto.BoardDetailDTO(
                  b.id, b.title, b.content, u.nickname, d.districtName, d.dongName, b.createdAt, b.viewCount
                )
                from Board b join b.user u join DongLocation d on d.dongCode = b.dongCode
                where b.id=:postId
            """)
    Optional<BoardDetailDTO> findDetailById(@Param("postId") Long postId);
}
