package com.gujin.lottery.service;

import com.gujin.lottery.constant.PrizeConstant;
import com.gujin.lottery.entity.Player;
import com.gujin.lottery.exception.LotteryError;
import com.gujin.lottery.exception.LotteryException;
import javafx.fxml.LoadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    @Autowired
    RedisTemplate redisTemplate;

    private Map<String, Player> firstMap = new ConcurrentHashMap<>();
    private Map<String, Player> lotteryMap = new ConcurrentHashMap<>();

    private volatile boolean isGaming = false;

    private List<Integer> prizePool = new ArrayList<>();

    /**
     * 开始游戏
     */
    public void joinGame(String id, String name) {
        if (isGaming) {
            throw new LotteryException(LotteryError.GAMING);
        }
        Player player = new Player();
        player.setName(name);
        lotteryMap.put(id, player);
    }

    /**
     * 开放抽取道具
     */
    public void openExtractingProps() {
        if (isGaming) {
            throw new LotteryException(LotteryError.GAMING);
        }
        isGaming = true;
        for (int i = 0; i < lotteryMap.size(); i++) {
            prizePool.add(i);
        }
    }

    /**
     * 抽取道具
     */
    public void extractingProps(String id) {
        if (!isGaming) {
            throw new LotteryException(LotteryError.GAME_NOT_STARTED);
        }

    }

    /**
     * 开始抽奖
     */
    public void startLotterring() {
    }

    /**
     * 道具分配
     */
    public void distributionProps(String id) {
        int size = lotteryMap.size();
        int firstAmount = size / PrizeConstant.FIRST_RATE;
        int secondAmount = size / PrizeConstant.SECOND_RATE;
        int thirdAmount = size / PrizeConstant.THIRD_RATE;
        Random random = new Random();
        String[] keyArray = lotteryMap.keySet().toArray(new String[size]);
        //创建存放已经获得二等奖或三等奖的玩家的key集合，防止玩家重复获相同的奖。
        Set<String> areadySecond = new HashSet<>();
        Set<String> areadyThird = new HashSet<>();

        for (int i = firstMap.size(); i < firstAmount;) {
            String randomKey = keyArray[random.nextInt(size) - 1];
            firstMap.put(randomKey, lotteryMap.get(randomKey));
        }
         //循环完删除保证抽取概率一样
        firstMap.forEach((k,v) ->lotteryMap.remove(k));
        //刷新size
        size = lotteryMap.size();
        for (int i = areadySecond.size(); i < secondAmount;) {
            String randomKey = keyArray[random.nextInt(size) - 1];
            lotteryMap.get(randomKey).setPrizeSign(PrizeConstant.SECOND);
            areadySecond.add(randomKey);
        }
        for (int i = areadyThird.size(); i < thirdAmount;) {
            String randomKey = keyArray[random.nextInt(size) - 1];
            Player player = lotteryMap.get(randomKey);
            //判断已经获得二等奖的玩家
            if(player.getPrizeSign().equals(PrizeConstant.SECOND)){
                player.setPrizeSign(PrizeConstant.SECOND_THIRD);
            }else{
                player.setPrizeSign(PrizeConstant.THIRD);
            }
            areadyThird.add(randomKey);
        }
    }
}
