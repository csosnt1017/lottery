package com.gujin.lottery.service;

import com.gujin.lottery.constant.PrizeConstant;
import com.gujin.lottery.entity.Player;
import com.gujin.lottery.entity.Rate;
import com.gujin.lottery.exception.LotteryError;
import com.gujin.lottery.exception.LotteryException;
import com.gujin.lottery.util.LotteryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class GameService {
    @Autowired
    RedisTemplate redisTemplate;

    private Map<String, Player> firstMap = new ConcurrentHashMap<>();
    private Map<String, Player> lotteryMap = new ConcurrentHashMap<>();
    private List<Rate> rateList = new CopyOnWriteArrayList<>();

    public volatile boolean isGaming = false;

    /**
     * 开始游戏
     */
    public Player joinGame(String id, String name) {
        Player player = new Player();
        player.setName(name);
        lotteryMap.put(id, player);
        return player;
    }

    /**
     * 开放抽取道具
     */
    public void openExtractingProps() {
        isGaming = true;
        distributionProps();
        calculatingProbability();
        sortPlayer();
    }

    /**
     * 抽取道具
     */
    public Player extractingProps(String id) {
        Player godPlayer = firstMap.get(id);
        Player ordinaryPlayer = lotteryMap.get(id);
        if (godPlayer != null) {
            return godPlayer;
        } else if (ordinaryPlayer != null) {
            return ordinaryPlayer;
        } else {
            return null;
        }
    }

    /**
     * 开始抽奖
     */
    public Player startLotterring() {
        List<Double> doubleList = new ArrayList<>();
        rateList.forEach(r -> {
            doubleList.add(r.getRate());
        });
        Rate rate = rateList.get(LotteryUtil.lottery(doubleList));
        return lotteryMap.get(rate.getId());
    }

    /**
     * @Description: 清除二等奖
     * @Author: gj
     * @Date: 2020/3/14
     */
    public Player clearPrize(String id) {
        Player player = lotteryMap.get(id);
        if (PrizeConstant.SECOND_THIRD == player.getPrizeSign()) {
            player.setPrizeSign(PrizeConstant.THIRD);
        } else if (PrizeConstant.SECOND == player.getPrizeSign()) {
            player.setPrizeSign(null);
        } else {
            throw new LotteryException(LotteryError.NOT_SECOND);
        }
        return player;
    }

    /**
     * @Description: 名字获取id
     * @Author: gj
     * @Date: 2020/3/21
     */
    public String getId(String name) {
        for (Map.Entry<String, Player> playerEntry : lotteryMap.entrySet()) {
            if (playerEntry.getValue().getName().equals(name)) {
                return playerEntry.getKey();
            }
        }
        return null;
    }

    /**
     * @Description: 结束游戏
     * @Author: gj
     * @Date: 2020/3/14
     */
    public void finishGame() {
        firstMap.clear();
        lotteryMap.clear();
        rateList.clear();
        isGaming = false;
    }

    /**
     * 道具分配
     */
    private void distributionProps() {
        int size = lotteryMap.size();
        int firstAmount = size / PrizeConstant.FIRST_RATE;
        int secondAmount = size / PrizeConstant.SECOND_RATE;
        int thirdAmount = size / PrizeConstant.THIRD_RATE;
        Random random = new Random();
        String[] keyArray = lotteryMap.keySet().toArray(new String[size]);
        //创建存放已经获得一等奖、二等奖或三等奖的玩家的key集合，防止玩家重复获相同的奖。
        Set<String> areadyFirst = new HashSet<>();
        Set<String> areadySecond = new HashSet<>();
        Set<String> areadyThird = new HashSet<>();

        for (int i = 0; i < firstAmount; ) {
            String randomKey = keyArray[random.nextInt(size)];
            areadyFirst.add(randomKey);
            i = areadyFirst.size();
        }
        areadyFirst.forEach(f -> {
            Player player = lotteryMap.get(f);
            player.setPrizeSign(PrizeConstant.FIRST);
            firstMap.put(f, player);
        });
        //循环完删除保证抽取概率一样
        firstMap.forEach((k, v) -> lotteryMap.remove(k));
        //刷新size和key数组
        size = lotteryMap.size();
        keyArray = lotteryMap.keySet().toArray(new String[size]);
        for (int i = 0; i < secondAmount; ) {
            String randomKey = keyArray[random.nextInt(size)];
            lotteryMap.get(randomKey).setPrizeSign(PrizeConstant.SECOND);
            areadySecond.add(randomKey);
            i = areadySecond.size();
        }
        for (int i = 0; i < thirdAmount; ) {
            String randomKey = keyArray[random.nextInt(size)];
            Player player = lotteryMap.get(randomKey);
            //判断已经获得二等奖的玩家
            if (player.getPrizeSign().equals(PrizeConstant.SECOND)) {
                player.setPrizeSign(PrizeConstant.SECOND_THIRD);
            } else {
                player.setPrizeSign(PrizeConstant.THIRD);
            }
            areadyThird.add(randomKey);
            i = areadyThird.size();
        }
    }

    /**
     * @Description: 计算概率
     * @Author: gj
     * @Date: 2020/3/9
     */
    private void calculatingProbability() {
        double rate = 1.00 / lotteryMap.size();
        int i = 0;
        for (Map.Entry<String, Player> map : lotteryMap.entrySet()) {
            if (map.getValue().getPrizeSign().equals(PrizeConstant.THIRD) || map.getValue().getPrizeSign().equals(PrizeConstant.SECOND_THIRD)) {
                i++;
                map.getValue().setRate(rate / 2);
            }
        }
        double newRate = rate;
        if (i != 0) {
            newRate = (rate / 2 * i) / (lotteryMap.size() - i) + rate;
        }
        double finalNewRate = newRate;
        lotteryMap.forEach((k, v) -> {
            if (v.getPrizeSign().equals(PrizeConstant.SECOND) || v.getPrizeSign().equals(0)) {
                v.setRate(finalNewRate);
            }
        });
    }

    /**
     * @Description: 玩家按概率排序
     * @Author: gj
     * @Date: 2020/3/9
     */
    private void sortPlayer() {
        lotteryMap.forEach((k, v) -> {
            Rate rate = new Rate();
            rate.setId(k);
            rate.setRate(v.getRate());
            rateList.add(rate);
        });
        rateList = rateList.stream().sorted(Comparator.comparing(Rate::getRate)).collect(Collectors.toList());
    }
}
