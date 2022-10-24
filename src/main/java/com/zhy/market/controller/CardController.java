package com.zhy.market.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhy.market.domain.Card;
import com.zhy.market.mapper.CardMapper;

@RestController
@RequestMapping("card")
public class CardController {

    @Resource
    private CardMapper cardMapper;

    @PostMapping("addCard")
    public Object addCard(@RequestBody Card card) {
        return cardMapper.addCard(card.getSsmc(), card.getGgxh(), card.getSyzt(), card.getSsgx(), card.getJsbh(),
                card.getJslx(), card.getJszb(), card.getYxzt(), card.getJsqk(), card.getKgfx(), card.getXdwz(),
                card.getJs(), card.getSccj(), card.getAzsj());
    }

    @GetMapping("getAllCard")
    public Object getAllCard() {
        return cardMapper.getAllCard();
    }   
}
