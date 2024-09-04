package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {
    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;
    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns =@JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")//반대쪽 엔티티 조인커럶 설정
    )//중간 매핑 테이블 이름을 선언해줘야한다.
    //하지만 이렇게 쓰면 필드 추가가 불가능해서 사용하지 않는다.
    private List<Item> items =new ArrayList<>();

    //카테고리 구조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    //
    @OneToMany(mappedBy = "parent")
    private List<Category> child=new ArrayList<>();

    //연관관계 메서드
    private void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }

}


