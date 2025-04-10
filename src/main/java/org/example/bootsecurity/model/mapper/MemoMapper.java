package org.example.bootsecurity.model.mapper;

import org.apache.ibatis.annotations.*;
import org.example.bootsecurity.model.domain.Memo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



/*
데이터베이스와 상호작용할 메서드를 정의하는 MyBatis 매퍼 인터페이스
* 이 인터페이스는 @Mapper 어노테이션을 사용하여
* Spring 컨테이너에 빈으로 등록됩니다.
* CRUD 작업은 @Select, @Insert, @Update, @Delete 어노테이션을
* 사용하여 SQL 쿼리와 매핑할 수 있습니다.
 * */

// Spring
@Repository
// MyBatis
@Mapper
public interface MemoMapper {

    @Select("SELECT * FROM memo ORDER BY id")
    List<Memo> findAll();

//    @Select("SELECT * FROM memo WHERE id = #{id}")
//    Optional<Memo> findById(Long id);

    @Select("SELECT * FROM memo WHERE text = %#{text}%")
    Optional<Memo> findByText(String text);


    @Insert("INSERT INTO memo(text) VALUES(#{text})")
//  Supabase는 알아서 Auto로 증가를 하고 있는데... 이걸 개입하려고 하니까...
//    @Options(useGeneratedKeys = true, keyProperty = "id") // 자동 생성된 ID 반환 설정
    void insert(Memo memo);

    @Update("UPDATE memo SET text=#{text} WHERE id=#{id}")
    void update(Memo memo);

    @Delete("DELETE FROM memo")
    void deleteAll();

    @Delete("DELETE FROM memo WHERE id = #{id}")
    void delete(Long id);

    @Select("SELECT * FROM memo WHERE id = #{id}")
    Memo findById(Long id);


//    @Update("UPDATE memo SET points = points + #{pointsToAdd} WHERE id = #{userId}")
//    int addPoints(@Param("userId") Long userId, @Param("pointsToAdd") int pointsToAdd);

//    @Update("UPDATE memo SET points = points - #{pointsToDeduct} WHERE id = #{userId} AND points >= #{pointsToDeduct}")
//    int deductPoints(@Param("userId") Long userId, @Param("pointsToDeduct") int pointsToDeduct);
}