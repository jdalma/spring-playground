package example.example01

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Repository

@Mapper
interface Example01PersonMapper {

    @Insert(
        """
            INSERT INTO PERSON(
                first_name,
                last_name,
                birth_date,
                employed,
                occupation
            ) VALUES (
                #{firstName},
                #{lastName},
                #{birthDate},
                #{employed},
                #{occupation}
            )
        """
    )
    fun save(person : Example01PersonModel) : Int

    @Select(
        """
            SELECT *
            FROM person
        """
    )
    fun select() : List<Example01PersonModel>

}
