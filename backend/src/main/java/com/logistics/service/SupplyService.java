package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.logistics.common.BusinessException;
import com.logistics.entity.SupplyInfo;
import com.logistics.entity.TraceInfo;
import com.logistics.mapper.SupplyInfoMapper;
import com.logistics.mapper.TraceInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplyService extends ServiceImpl<SupplyInfoMapper, SupplyInfo> {

    private final TraceInfoMapper traceInfoMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ========== 供应方操作 ==========

    /**
     * 发布供应信息
     */
    @Transactional
    public SupplyInfo addSupply(SupplyInfo supply, TraceInfo traceInfo) {
        // 生成推荐价格
        BigDecimal recommendPrice = calculateRecommendPrice(supply.getCategory(), supply.getProductionPlace());
        supply.setRecommendPrice(recommendPrice != null ? recommendPrice : BigDecimal.ZERO);
        supply.setAuditStatus(0); // 待审核
        supply.setStatus(0); // 默认下架
        supply.setDeleteFlag(0);
        save(supply);

        // 保存溯源信息
        if (traceInfo != null) {
            traceInfo.setSupplyId(supply.getSupplyId());
            traceInfoMapper.insert(traceInfo);
        }

        // 生成溯源码
        String traceCode = generateTraceCode(supply.getSupplyId());
        String traceCodeUrl = generateQRCode(traceCode, supply.getSupplyId());
        supply.setTraceCode(traceCode);
        supply.setTraceCodeUrl(traceCodeUrl);
        updateById(supply);

        return supply;
    }

    /**
     * 编辑供应信息
     */
    public void updateSupply(SupplyInfo supply) {
        SupplyInfo existing = getById(supply.getSupplyId());
        if (existing == null)
            throw new BusinessException("供应信息不存在");

        SupplyInfo update = new SupplyInfo();
        update.setSupplyId(supply.getSupplyId());
        update.setProductName(supply.getProductName());
        update.setPrice(supply.getPrice());
        update.setStock(supply.getStock());
        update.setProductionPlace(supply.getProductionPlace());
        update.setProductionDate(supply.getProductionDate());
        update.setShelfLife(supply.getShelfLife());
        updateById(update);
    }

    /**
     * 供应方查询自有供应列表
     */
    public Page<SupplyInfo> supplierList(int page, int size, Long supplierId, String productName, Integer status) {
        LambdaQueryWrapper<SupplyInfo> w = new LambdaQueryWrapper<>();
        w.eq(SupplyInfo::getSupplierId, supplierId)
                .eq(SupplyInfo::getDeleteFlag, 0)
                .like(StringUtils.hasText(productName), SupplyInfo::getProductName, productName)
                .eq(status != null, SupplyInfo::getStatus, status)
                .orderByDesc(SupplyInfo::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    /** 上架 */
    public void online(Long supplyId, Long supplierId) {
        SupplyInfo s = getById(supplyId);
        if (s == null || !s.getSupplierId().equals(supplierId))
            throw new BusinessException("供应信息不存在");
        if (s.getAuditStatus() != 1)
            throw new BusinessException("只有审核通过的供应信息才能上架");
        SupplyInfo u = new SupplyInfo();
        u.setSupplyId(supplyId);
        u.setStatus(1);
        updateById(u);
    }

    /** 下架 */
    public void offline(Long supplyId, Long supplierId) {
        SupplyInfo s = getById(supplyId);
        if (s == null || !s.getSupplierId().equals(supplierId))
            throw new BusinessException("供应信息不存在");
        SupplyInfo u = new SupplyInfo();
        u.setSupplyId(supplyId);
        u.setStatus(0);
        updateById(u);
    }

    // ========== 公共查询 ==========

    /** 全角色查询上架供应列表 */
    public Page<SupplyInfo> commonList(int page, int size, String productName, String category,
            String productionPlace, BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<SupplyInfo> w = new LambdaQueryWrapper<>();
        w.eq(SupplyInfo::getDeleteFlag, 0)
                .eq(SupplyInfo::getAuditStatus, 1)
                .eq(SupplyInfo::getStatus, 1)
                .like(StringUtils.hasText(productName), SupplyInfo::getProductName, productName)
                .eq(StringUtils.hasText(category), SupplyInfo::getCategory, category)
                .like(StringUtils.hasText(productionPlace), SupplyInfo::getProductionPlace, productionPlace)
                .ge(minPrice != null, SupplyInfo::getPrice, minPrice)
                .le(maxPrice != null, SupplyInfo::getPrice, maxPrice)
                .orderByDesc(SupplyInfo::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    /** 供应详情 + 溯源信息（supplyId 和 traceCode 二选一） */
    public Map<String, Object> getDetail(Long supplyId, String traceCode) {
        SupplyInfo supply;
        if (supplyId != null) {
            supply = getById(supplyId);
        } else if (traceCode != null && !traceCode.isBlank()) {
            supply = lambdaQuery().eq(SupplyInfo::getTraceCode, traceCode)
                    .eq(SupplyInfo::getDeleteFlag, 0).one();
        } else {
            throw new BusinessException("请提供 supplyId 或 traceCode");
        }
        if (supply == null || supply.getDeleteFlag() == 1)
            throw new BusinessException("供应信息不存在");
        TraceInfo trace = traceInfoMapper.selectOne(
                new LambdaQueryWrapper<TraceInfo>().eq(TraceInfo::getSupplyId, supply.getSupplyId()));
        Map<String, Object> result = new HashMap<>();
        result.put("supply", supply);
        result.put("trace", trace);
        return result;
    }

    // ========== 管理员操作 ==========

    /** 管理员审核 */
    public void audit(Long supplyId, Integer auditStatus, String auditRemark) {
        SupplyInfo s = getById(supplyId);
        if (s == null)
            throw new BusinessException("供应信息不存在");
        SupplyInfo u = new SupplyInfo();
        u.setSupplyId(supplyId);
        u.setAuditStatus(auditStatus);
        // 审核通过自动上架
        if (auditStatus == 1)
            u.setStatus(1);
        updateById(u);
    }

    /** 管理员强制下架 */
    public void forceOffline(Long supplyId) {
        SupplyInfo u = new SupplyInfo();
        u.setSupplyId(supplyId);
        u.setStatus(0);
        updateById(u);
    }

    /** 管理员查询全量（含待审核） */
    public Page<SupplyInfo> adminList(int page, int size, String productName, Integer auditStatus) {
        LambdaQueryWrapper<SupplyInfo> w = new LambdaQueryWrapper<>();
        w.eq(SupplyInfo::getDeleteFlag, 0)
                .like(StringUtils.hasText(productName), SupplyInfo::getProductName, productName)
                .eq(auditStatus != null, SupplyInfo::getAuditStatus, auditStatus)
                .orderByDesc(SupplyInfo::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    // ========== 内部方法 ==========

    /** 推荐价格：同品类同产地近30天成交均价 */
    private BigDecimal calculateRecommendPrice(String category, String productionPlace) {
        LambdaQueryWrapper<SupplyInfo> w = new LambdaQueryWrapper<>();
        w.eq(SupplyInfo::getCategory, category)
                .eq(SupplyInfo::getAuditStatus, 1)
                .ge(SupplyInfo::getCreateTime, LocalDate.now().minusDays(30));
        if (StringUtils.hasText(productionPlace)) {
            w.like(SupplyInfo::getProductionPlace, productionPlace);
        }
        List<SupplyInfo> list = list(w);
        if (list.isEmpty())
            return null;

        BigDecimal sum = list.stream().map(SupplyInfo::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
    }

    /** 生成溯源码编号 */
    private String generateTraceCode(Long supplyId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "TC" + date + String.format("%06d", supplyId % 1000000);
    }

    /** 生成溯源二维码图片（本地存储） */
    private String generateQRCode(String traceCode, Long supplyId) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String dir = uploadDir + "/qrcode/" + datePath;
            new File(dir).mkdirs();
            String fileName = "trace_" + supplyId + ".png";

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(traceCode, BarcodeFormat.QR_CODE, 300, 300, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ImageIO.write(image, "PNG", Path.of(dir, fileName).toFile());

            return "/files/qrcode/" + datePath + "/" + fileName;
        } catch (Exception e) {
            throw new BusinessException("溯源码生成失败: " + e.getMessage());
        }
    }
}
