package com.gujin.lottery.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright © 2020年 lottery. All rights reserved.
 *
 * @author 古今
 * <p>
 * 抽奖工具类
 * @date 2020/3/6
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2020/3/6   古今   v1.0.0       新增
 */
public class LotteryUtil {
    public static int lottery(List<Double> rateList) {
        if (null != rateList && rateList.size() > 0) {
            return draw(rateList);
        }
        return -1;
    }

    public static int draw(List<Double> rateList) {
        List<Double> sortRateList = new ArrayList<>();
        // 计算概率总和
        Double sumRate = 0D;
        for (Double prob : rateList) {
            sumRate += prob;
        }
        if (sumRate != 0) {
            double rate = 0D;   //概率所占比例
            for (Double prob : rateList) {
                rate += prob;
                // 构建一个比例区段组成的集合(避免概率和不为1)
                sortRateList.add(rate / sumRate);
            }

            // 获取中奖的index
            while (true) {
                double random = Math.random();
                sortRateList.add(random);
                Collections.sort(sortRateList);
                int index = sortRateList.indexOf(random);
                if (index != 0) {
                    //随机数在最后面则重抽
                    if (index == sortRateList.size() - 1) {
                        sortRateList.remove(random);
                    }else {
                        return index;
                    }
                }
            }
        }
        return -1;
    }
}
