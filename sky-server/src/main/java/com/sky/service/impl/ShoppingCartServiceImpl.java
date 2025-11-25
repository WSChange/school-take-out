package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {


        // 判断当前加入购物车中的商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        // 获取用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //采用List集合延展性更好，查shoppingCart表可能只有一条数据，但查用户购物车数据需要查询当前用户购物车中的所有数据。
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 若已经存在，商品数量+1
        if (list != null && list.size() > 0){
            // 这里要么查不到数据，要么只能查到一条数据，因为根据setmealId或dishId只能查到商品是否在购物车中，要么在要么不在，因此最多只能返回一条数据
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            // update shopping_cart set number = ? where id = ?
            shoppingCartMapper.updateNumberById(cart);
        }else {
            // 若不存在，需要插入一条购物车数据
            // 判断添加购物车中的商品是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null){
                // 本次添加到购物车的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                // 本次添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }
}
