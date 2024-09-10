package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }
    //정보 수정(더티체크)
//    @Transactional
//    public void updateItem(Long itemId, Book bookParam){
//        Item findItem=itemRepository.findOne(itemId);
//        findItem.setPrice(bookParam.getPrice());
//        findItem.setName(bookParam.getName());
//        findItem.setStockQuantity(bookParam.getStockQuantity());
//        //이대로 종료하면 트랜젝션이 끝나면서 자동으로 flush된다.
//    }
    @Transactional
    public void updateItem(Long itemId, String name,int price,int stockQuentity){
        Item findItem=itemRepository.findOne(itemId);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuentity);
        //이대로 종료하면 트랜젝션이 끝나면서 자동으로 flush된다.
    }

    //전체 조회
    public List<Item> findItems(){
        return itemRepository.findAll();
    }
    //단건 조회
    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
