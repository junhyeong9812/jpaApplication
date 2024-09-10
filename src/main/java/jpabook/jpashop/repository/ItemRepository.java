package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;
    //정보 저장
    public void save(Item item){
        if(item.getId()==null){
            em.persist(item);
        }else {
            em.merge(item);
            //merge는 DB에서 item을 찾아서 파라미터로 넘어온 item으로 값을 바꿔버린다.
            //그 후 트렌젝션에서 플러쉬가 되는건데
            //이 때 문제는 모든 데이터가 덮어씌워지는 것이다.
            //하지만 이렇게 넘어온 item은 영속성으로 관리가 되는 건 아니다.
        }
    }

    //단건 조회
    public Item findOne(Long id){
        return em.find(Item.class,id);
    }
        
    //목록 조회
    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class).getResultList();

    }
}
