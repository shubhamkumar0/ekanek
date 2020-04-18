package com.example.ekanek;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface FileRepository extends CrudRepository<Files, Long> {
    public List<Files> findByEmail(String email);

    @Query(value = "insert into `filedetails` (`name`, `email`, `link`, `desc`) VALUES (:name,:email,:link,:desc)", nativeQuery = true)
    @Transactional
    @Modifying
    void saveFile(@Param("name") String name,@Param("email") String email,@Param("link") String link,@Param("desc") String desc);

}
