package com.logistics.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.aop.OperLog;
import com.logistics.entity.OrderMain;
import com.logistics.entity.SupplyInfo;
import com.logistics.mapper.OrderMainMapper;
import com.logistics.mapper.SupplyInfoMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

        private final OrderMainMapper orderMainMapper;
        private final SupplyInfoMapper supplyInfoMapper;

        // ==================== 订单导出 ====================

        @Data
        static class OrderExportDTO {
                @ExcelProperty("订单编号")
                @ColumnWidth(22)
                private String orderNo;

                @ExcelProperty("消费者")
                private String consumerName;

                @ExcelProperty("供应方")
                private String supplierName;

                @ExcelProperty("物流方")
                private String logisticsName;

                @ExcelProperty("商品金额(元)")
                private BigDecimal productAmount;

                @ExcelProperty("物流费用(元)")
                private BigDecimal logisticsAmount;

                @ExcelProperty("订单总额(元)")
                private BigDecimal totalAmount;

                @ExcelProperty("订单状态")
                private String orderStatusText;

                @ExcelProperty("支付状态")
                private String payStatusText;

                @ExcelProperty("收货地址")
                @ColumnWidth(30)
                private String deliveryAddress;

                @ExcelProperty("物流单号")
                private String deliveryNo;

                @ExcelProperty("下单时间")
                @ColumnWidth(20)
                private LocalDateTime createTime;

                @ExcelProperty("支付时间")
                @ColumnWidth(20)
                private LocalDateTime payTime;

                @ExcelProperty("完成时间")
                @ColumnWidth(20)
                private LocalDateTime finishTime;
        }

        private static String orderStatusText(Integer status) {
                if (status == null)
                        return "";
                return switch (status) {
                        case 0 -> "待支付";
                        case 1 -> "待发货";
                        case 2 -> "待揽收";
                        case 3 -> "运输中";
                        case 4 -> "待收货";
                        case 5 -> "已完成";
                        case 99 -> "已关闭";
                        default -> String.valueOf(status);
                };
        }

        private static String payStatusText(Integer status) {
                if (status == null)
                        return "";
                return switch (status) {
                        case 0 -> "未支付";
                        case 1 -> "已支付";
                        case 2 -> "已退款";
                        default -> String.valueOf(status);
                };
        }

        @GetMapping("/orders")
        public void exportOrders(HttpServletResponse response,
                        @RequestParam(required = false) Integer orderStatus) throws IOException {
                LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<OrderMain>()
                                .eq(OrderMain::getDeleteFlag, 0)
                                .eq(orderStatus != null, OrderMain::getOrderStatus, orderStatus)
                                .orderByDesc(OrderMain::getCreateTime);
                List<OrderMain> list = orderMainMapper.selectList(wrapper);

                List<OrderExportDTO> exportList = list.stream().map(o -> {
                        OrderExportDTO dto = new OrderExportDTO();
                        dto.setOrderNo(o.getOrderNo());
                        dto.setConsumerName(o.getConsumerName());
                        dto.setSupplierName(o.getSupplierName());
                        dto.setLogisticsName(o.getLogisticsName() != null ? o.getLogisticsName() : "--");
                        dto.setProductAmount(o.getProductAmount());
                        dto.setLogisticsAmount(o.getLogisticsAmount());
                        dto.setTotalAmount(o.getTotalAmount());
                        dto.setOrderStatusText(orderStatusText(o.getOrderStatus()));
                        dto.setPayStatusText(payStatusText(o.getPayStatus()));
                        dto.setDeliveryAddress(o.getDeliveryAddress());
                        dto.setDeliveryNo(o.getDeliveryNo() != null ? o.getDeliveryNo() : "--");
                        dto.setCreateTime(o.getCreateTime());
                        dto.setPayTime(o.getPayTime());
                        dto.setFinishTime(o.getFinishTime());
                        return dto;
                }).collect(Collectors.toList());

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("订单数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

                EasyExcel.write(response.getOutputStream(), OrderExportDTO.class)
                                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                                .sheet("订单列表")
                                .doWrite(exportList);
        }

        // ==================== 供应数据导出 ====================

        @Data
        static class SupplyExportDTO {
                @ExcelProperty("产品名称")
                @ColumnWidth(20)
                private String productName;

                @ExcelProperty("品类")
                private String category;

                @ExcelProperty("子品类")
                private String subCategory;

                @ExcelProperty("产地")
                @ColumnWidth(18)
                private String productionPlace;

                @ExcelProperty("生产日期")
                private String productionDate;

                @ExcelProperty("保质期(天)")
                private Integer shelfLife;

                @ExcelProperty("单价(元/kg)")
                private BigDecimal price;

                @ExcelProperty("推荐价(元/kg)")
                private BigDecimal recommendPrice;

                @ExcelProperty("库存(kg)")
                private BigDecimal stock;

                @ExcelProperty("供应方")
                private String supplierName;

                @ExcelProperty("溯源码")
                private String traceCode;

                @ExcelProperty("审核状态")
                private String auditStatusText;

                @ExcelProperty("上架状态")
                private String statusText;

                @ExcelProperty("发布时间")
                @ColumnWidth(20)
                private LocalDateTime createTime;
        }

        @GetMapping("/supplies")
        public void exportSupplies(HttpServletResponse response,
                        @RequestParam(required = false) Integer auditStatus) throws IOException {
                LambdaQueryWrapper<SupplyInfo> wrapper = new LambdaQueryWrapper<SupplyInfo>()
                                .eq(SupplyInfo::getDeleteFlag, 0)
                                .eq(auditStatus != null, SupplyInfo::getAuditStatus, auditStatus)
                                .orderByDesc(SupplyInfo::getCreateTime);
                List<SupplyInfo> list = supplyInfoMapper.selectList(wrapper);

                List<SupplyExportDTO> exportList = list.stream().map(s -> {
                        SupplyExportDTO dto = new SupplyExportDTO();
                        dto.setProductName(s.getProductName());
                        dto.setCategory(s.getCategory());
                        dto.setSubCategory(s.getSubCategory());
                        dto.setProductionPlace(s.getProductionPlace());
                        dto.setProductionDate(s.getProductionDate() != null ? s.getProductionDate().toString() : "");
                        dto.setShelfLife(s.getShelfLife());
                        dto.setPrice(s.getPrice());
                        dto.setRecommendPrice(s.getRecommendPrice());
                        dto.setStock(s.getStock());
                        dto.setSupplierName(s.getSupplierName());
                        dto.setTraceCode(s.getTraceCode());
                        dto.setAuditStatusText(s.getAuditStatus() == null ? ""
                                        : s.getAuditStatus() == 1 ? "已通过" : s.getAuditStatus() == 2 ? "已驳回" : "待审核");
                        dto.setStatusText(s.getStatus() == null ? "" : s.getStatus() == 1 ? "上架" : "下架");
                        dto.setCreateTime(s.getCreateTime());
                        return dto;
                }).collect(Collectors.toList());

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("供应数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

                EasyExcel.write(response.getOutputStream(), SupplyExportDTO.class)
                                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                                .sheet("供应列表")
                                .doWrite(exportList);
        }
}
