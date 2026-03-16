package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.entity.OrderMain;
import com.logistics.entity.SupplyInfo;
import com.logistics.mapper.OrderMainMapper;
import com.logistics.mapper.SupplyInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务 - 提供各维度统计数据
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

        private final SupplyInfoMapper supplyInfoMapper;
        private final OrderMainMapper orderMainMapper;

        /**
         * 总览统计卡片数据
         */
        public Map<String, Object> overview() {
                Map<String, Object> data = new LinkedHashMap<>();
                // 供应信息总数
                data.put("totalSupply", supplyInfoMapper.selectCount(
                                new LambdaQueryWrapper<SupplyInfo>().eq(SupplyInfo::getDeleteFlag, 0)));
                // 上架中
                data.put("onlineSupply", supplyInfoMapper.selectCount(
                                new LambdaQueryWrapper<SupplyInfo>().eq(SupplyInfo::getDeleteFlag, 0)
                                                .eq(SupplyInfo::getStatus, 1)));
                // 订单总数
                data.put("totalOrder", orderMainMapper.selectCount(
                                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getDeleteFlag, 0)));
                // 已完成订单
                data.put("finishedOrder", orderMainMapper.selectCount(
                                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getDeleteFlag, 0)
                                                .eq(OrderMain::getOrderStatus, 5)));
                // 总交易额
                List<OrderMain> finishedOrders = orderMainMapper.selectList(
                                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getDeleteFlag, 0)
                                                .eq(OrderMain::getOrderStatus, 5));
                BigDecimal totalAmount = finishedOrders.stream()
                                .map(OrderMain::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                data.put("totalAmount", totalAmount);
                return data;
        }

        /**
         * 近7天订单趋势（按日统计订单数）
         */
        public List<Map<String, Object>> orderTrend() {
                LocalDate end = LocalDate.now();
                LocalDate start = end.minusDays(6);
                List<Map<String, Object>> result = new ArrayList<>();

                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                        LocalDateTime dayStart = d.atStartOfDay();
                        LocalDateTime dayEnd = d.plusDays(1).atStartOfDay();
                        long count = orderMainMapper.selectCount(
                                        new LambdaQueryWrapper<OrderMain>()
                                                        .eq(OrderMain::getDeleteFlag, 0)
                                                        .ge(OrderMain::getCreateTime, dayStart)
                                                        .lt(OrderMain::getCreateTime, dayEnd));
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("date", d.format(DateTimeFormatter.ofPattern("MM-dd")));
                        item.put("count", count);
                        result.add(item);
                }
                return result;
        }

        /**
         * 品类分布（饼图）
         */
        public List<Map<String, Object>> categoryDistribution() {
                List<SupplyInfo> all = supplyInfoMapper.selectList(
                                new LambdaQueryWrapper<SupplyInfo>().eq(SupplyInfo::getDeleteFlag, 0));
                Map<String, Long> grouped = all.stream()
                                .collect(Collectors.groupingBy(SupplyInfo::getCategory, Collectors.counting()));
                List<Map<String, Object>> result = new ArrayList<>();
                grouped.forEach((k, v) -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("name", k);
                        item.put("value", v);
                        result.add(item);
                });
                return result;
        }

        /**
         * 产地排行 Top10（柱状图）
         */
        public List<Map<String, Object>> placeRanking() {
                List<SupplyInfo> all = supplyInfoMapper.selectList(
                                new LambdaQueryWrapper<SupplyInfo>().eq(SupplyInfo::getDeleteFlag, 0)
                                                .eq(SupplyInfo::getStatus, 1));
                Map<String, Long> grouped = all.stream()
                                .collect(Collectors.groupingBy(SupplyInfo::getProductionPlace, Collectors.counting()));
                return grouped.entrySet().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(10)
                                .map(e -> {
                                        Map<String, Object> item = new LinkedHashMap<>();
                                        item.put("place", e.getKey());
                                        item.put("count", e.getValue());
                                        return item;
                                })
                                .collect(Collectors.toList());
        }

        /**
         * 供应方交易额排行 Top10
         */
        public List<Map<String, Object>> supplierRanking() {
                List<OrderMain> finished = orderMainMapper.selectList(
                                new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getDeleteFlag, 0)
                                                .eq(OrderMain::getOrderStatus, 5));
                Map<String, BigDecimal> grouped = finished.stream()
                                .collect(Collectors.groupingBy(OrderMain::getSupplierName,
                                                Collectors.reducing(BigDecimal.ZERO, OrderMain::getTotalAmount,
                                                                BigDecimal::add)));
                return grouped.entrySet().stream()
                                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                                .limit(10)
                                .map(e -> {
                                        Map<String, Object> item = new LinkedHashMap<>();
                                        item.put("name", e.getKey());
                                        item.put("amount", e.getValue());
                                        return item;
                                })
                                .collect(Collectors.toList());
        }

        /**
         * 库存预警列表（低于安全库存阈值 100kg 的上架商品）
         */
        public List<Map<String, Object>> inventoryWarning() {
                BigDecimal threshold = new BigDecimal("100");
                List<SupplyInfo> list = supplyInfoMapper.selectList(
                                new LambdaQueryWrapper<SupplyInfo>()
                                                .eq(SupplyInfo::getDeleteFlag, 0)
                                                .eq(SupplyInfo::getStatus, 1)
                                                .le(SupplyInfo::getStock, threshold));
                return list.stream().map(s -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("productName", s.getProductName());
                        item.put("supplierName", s.getSupplierName());
                        item.put("stock", s.getStock());
                        item.put("category", s.getCategory());
                        return item;
                }).collect(Collectors.toList());
        }

        /**
         * 冷链异常率统计（按时间统计运输超时的订单）
         */
        public Map<String, Object> coldChainWarning() {
                // 总运输中订单
                long totalTransport = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                                .eq(OrderMain::getDeleteFlag, 0)
                                .in(OrderMain::getOrderStatus, Arrays.asList(2, 3))); // 运输中+待收货

                // 异常冷链订单：运输中且状态超过一定时间的模拟（超3天）
                LocalDateTime threshold = LocalDateTime.now().minusDays(3);
                long exceptionCount = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                                .eq(OrderMain::getDeleteFlag, 0)
                                .in(OrderMain::getOrderStatus, Arrays.asList(2, 3))
                                .lt(OrderMain::getCreateTime, threshold));

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("totalTransport", totalTransport);
                result.put("exceptionCount", exceptionCount);
                // 如果数据全为空，异常率为 0，不提供虚假的兜底演示数据
                double rate = totalTransport == 0 ? 0.0 : ((double) exceptionCount / totalTransport * 100);
                result.put("exceptionRate", rate);
                return result;
        }

        /**
         * 商品库存周转率监控 (出库/入库情况演示)
         */
        public List<Map<String, Object>> inventoryTurnover() {
                // 获取所有在架商品
                List<SupplyInfo> allActiveSupplies = supplyInfoMapper.selectList(
                                new LambdaQueryWrapper<SupplyInfo>()
                                                .eq(SupplyInfo::getDeleteFlag, 0)
                                                .eq(SupplyInfo::getStatus, 1));

                // 按品类分组
                Map<String, List<SupplyInfo>> grouped = allActiveSupplies.stream()
                                .filter(s -> s.getCategory() != null)
                                .collect(Collectors.groupingBy(SupplyInfo::getCategory));

                LocalDate today = LocalDate.now();
                List<Map<String, Object>> result = new ArrayList<>();

                for (Map.Entry<String, List<SupplyInfo>> entry : grouped.entrySet()) {
                        String category = entry.getKey();
                        List<SupplyInfo> supplies = entry.getValue();

                        // 计算该品类下的库存平均周转天数（当前日期 - 上架日期 的均值）
                        double avgDays = supplies.stream()
                                        .mapToLong(s -> {
                                                LocalDate createDate = s.getCreateTime() != null
                                                                ? s.getCreateTime().toLocalDate()
                                                                : today;
                                                long days = java.time.temporal.ChronoUnit.DAYS.between(createDate,
                                                                today);
                                                return days > 0 ? days : 1; // 至少为 1 天
                                        })
                                        .average()
                                        .orElse(1.0);

                        Map<String, Object> map = new LinkedHashMap<>();
                        // 如果数据库中存的是字典值"1"或"2"，做一个安全的映射回显
                        String catName = category;
                        if ("1".equals(category)) {
                                catName = "农产品";
                        } else if ("2".equals(category)) {
                                catName = "生产物料";
                        }

                        map.put("category", catName);
                        map.put("turnoverDays", Math.round(avgDays * 10.0) / 10.0);
                        result.add(map);
                }

                return result;
        }
}
