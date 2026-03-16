package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.logistics.common.BusinessException;
import com.logistics.entity.DemandAccept;
import com.logistics.entity.DemandMain;
import com.logistics.entity.SupplyInfo;
import com.logistics.mapper.DemandAcceptMapper;
import com.logistics.mapper.DemandMainMapper;
import com.logistics.mapper.SupplyInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DemandService extends ServiceImpl<DemandMainMapper, DemandMain> {

    private final DemandAcceptMapper acceptMapper;
    private final SupplyInfoMapper supplyInfoMapper;

    // ========== 采购方（消费者）操作 ==========

    /** 发布采购需求 */
    @Transactional
    public DemandMain addDemand(DemandMain demand) {
        demand.setDemandNo(generateDemandNo());
        demand.setAuditStatus(0);
        demand.setDemandStatus(0);
        demand.setIsTop(0);
        demand.setDeleteFlag(0);
        save(demand);
        return demand;
    }

    /** 编辑需求 */
    public void updateDemand(DemandMain demand, Long publisherId) {
        DemandMain existing = getById(demand.getDemandId());
        if (existing == null || !existing.getPublisherId().equals(publisherId))
            throw new BusinessException("需求不存在或无权操作");
        if (existing.getDemandStatus() >= 2)
            throw new BusinessException("承接中或已完成的需求不可编辑");

        DemandMain update = new DemandMain();
        update.setDemandId(demand.getDemandId());
        update.setTitle(demand.getTitle());
        update.setDescription(demand.getDescription());
        update.setQuantity(demand.getQuantity());
        update.setExpectPrice(demand.getExpectPrice());
        update.setExpectTime(demand.getExpectTime());
        updateById(update);
    }

    /** 采购方查询自有需求列表 */
    public Page<DemandMain> myList(int page, int size, Long publisherId, String title, Integer demandStatus) {
        LambdaQueryWrapper<DemandMain> w = new LambdaQueryWrapper<>();
        w.eq(DemandMain::getPublisherId, publisherId)
                .eq(DemandMain::getDeleteFlag, 0)
                .like(StringUtils.hasText(title), DemandMain::getTitle, title)
                .eq(demandStatus != null, DemandMain::getDemandStatus, demandStatus)
                .orderByDesc(DemandMain::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    /** 需求上架 */
    public void online(Long demandId, Long publisherId) {
        DemandMain d = getById(demandId);
        if (d == null || !d.getPublisherId().equals(publisherId))
            throw new BusinessException("需求不存在或无权操作");
        if (d.getAuditStatus() != 1)
            throw new BusinessException("只有审核通过的需求才能上架");
        DemandMain u = new DemandMain();
        u.setDemandId(demandId);
        u.setDemandStatus(1);
        updateById(u);
    }

    /** 需求下架 */
    public void offline(Long demandId, Long publisherId) {
        DemandMain d = getById(demandId);
        if (d == null || !d.getPublisherId().equals(publisherId))
            throw new BusinessException("需求不存在或无权操作");
        DemandMain u = new DemandMain();
        u.setDemandId(demandId);
        u.setDemandStatus(0);
        updateById(u);
    }

    /** 关闭需求 */
    public void close(Long demandId, Long publisherId, String closeReason) {
        DemandMain d = getById(demandId);
        if (d == null || !d.getPublisherId().equals(publisherId))
            throw new BusinessException("需求不存在或无权操作");
        DemandMain u = new DemandMain();
        u.setDemandId(demandId);
        u.setDemandStatus(99);
        u.setCloseReason(closeReason);
        updateById(u);
    }

    // ========== 公共查询 ==========

    /** 公开需求列表 */
    public Page<DemandMain> commonList(int page, int size, String title, String category,
            String region, LocalDate expectTimeStart, LocalDate expectTimeEnd) {
        LambdaQueryWrapper<DemandMain> w = new LambdaQueryWrapper<>();
        w.eq(DemandMain::getDeleteFlag, 0)
                .eq(DemandMain::getAuditStatus, 1)
                .in(DemandMain::getDemandStatus, 1, 2) // 已发布 or 承接中
                .like(StringUtils.hasText(title), DemandMain::getTitle, title)
                .eq(StringUtils.hasText(category), DemandMain::getCategory, category)
                .like(StringUtils.hasText(region), DemandMain::getAddress, region)
                .ge(expectTimeStart != null, DemandMain::getExpectTime, expectTimeStart)
                .le(expectTimeEnd != null, DemandMain::getExpectTime, expectTimeEnd)
                .orderByDesc(DemandMain::getIsTop)
                .orderByDesc(DemandMain::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    /** 需求详情 + 承接信息 */
    public Map<String, Object> getDetail(Long demandId) {
        DemandMain demand = getById(demandId);
        if (demand == null || demand.getDeleteFlag() == 1)
            throw new BusinessException("需求不存在");
        List<DemandAccept> accepts = acceptMapper.selectList(
                new LambdaQueryWrapper<DemandAccept>()
                        .eq(DemandAccept::getDemandId, demandId)
                        .eq(DemandAccept::getDeleteFlag, 0)
                        .orderByDesc(DemandAccept::getCreateTime));
        Map<String, Object> result = new HashMap<>();
        result.put("demand", demand);
        result.put("accepts", accepts);
        return result;
    }

    // ========== 供应方承接 ==========

    /** 供应方发起承接 */
    @Transactional
    public DemandAccept addAccept(DemandAccept accept) {
        DemandMain demand = getById(accept.getDemandId());
        if (demand == null || demand.getDeleteFlag() == 1)
            throw new BusinessException("需求不存在");
        if (demand.getDemandStatus() != 1 && demand.getDemandStatus() != 2)
            throw new BusinessException("该需求当前不接受承接");

        // 检查重复承接
        Long count = acceptMapper.selectCount(
                new LambdaQueryWrapper<DemandAccept>()
                        .eq(DemandAccept::getDemandId, accept.getDemandId())
                        .eq(DemandAccept::getAcceptSupplierId, accept.getAcceptSupplierId())
                        .eq(DemandAccept::getDeleteFlag, 0)
                        .ne(DemandAccept::getAcceptStatus, 2)); // 排除已驳回的
        if (count > 0)
            throw new BusinessException("您已提交过承接申请");

        accept.setAcceptStatus(0);
        accept.setDeleteFlag(0);
        acceptMapper.insert(accept);

        // 更新需求状态为承接中
        if (demand.getDemandStatus() == 1) {
            DemandMain u = new DemandMain();
            u.setDemandId(demand.getDemandId());
            u.setDemandStatus(2);
            updateById(u);
        }
        return accept;
    }

    /** 承接申请列表 */
    public Page<DemandAccept> acceptList(int page, int size, Long demandId, Integer acceptStatus) {
        LambdaQueryWrapper<DemandAccept> w = new LambdaQueryWrapper<>();
        w.eq(DemandAccept::getDeleteFlag, 0)
                .eq(demandId != null, DemandAccept::getDemandId, demandId)
                .eq(acceptStatus != null, DemandAccept::getAcceptStatus, acceptStatus)
                .orderByDesc(DemandAccept::getCreateTime);
        return acceptMapper.selectPage(new Page<>(page, size), w);
    }

    /** 确认承接 */
    @Transactional
    public void confirmAccept(Long acceptId, Long publisherId) {
        DemandAccept accept = acceptMapper.selectById(acceptId);
        if (accept == null)
            throw new BusinessException("承接申请不存在");
        DemandMain demand = getById(accept.getDemandId());
        if (demand == null || !demand.getPublisherId().equals(publisherId))
            throw new BusinessException("无权操作");
        if (accept.getAcceptStatus() != 0)
            throw new BusinessException("该申请已处理");

        // 确认该承接
        DemandAccept u = new DemandAccept();
        u.setAcceptId(acceptId);
        u.setAcceptStatus(1);
        acceptMapper.updateById(u);

        // 驳回其他待审核的承接
        List<DemandAccept> others = acceptMapper.selectList(
                new LambdaQueryWrapper<DemandAccept>()
                        .eq(DemandAccept::getDemandId, accept.getDemandId())
                        .eq(DemandAccept::getAcceptStatus, 0)
                        .ne(DemandAccept::getAcceptId, acceptId));
        for (DemandAccept other : others) {
            DemandAccept reject = new DemandAccept();
            reject.setAcceptId(other.getAcceptId());
            reject.setAcceptStatus(2);
            reject.setRejectReason("需求方已选择其他供应方");
            acceptMapper.updateById(reject);
        }

        // 更新需求状态为已对接
        DemandMain du = new DemandMain();
        du.setDemandId(accept.getDemandId());
        du.setDemandStatus(3);
        updateById(du);
    }

    /** 驳回承接 */
    public void rejectAccept(Long acceptId, Long publisherId, String rejectReason) {
        DemandAccept accept = acceptMapper.selectById(acceptId);
        if (accept == null)
            throw new BusinessException("承接申请不存在");
        DemandMain demand = getById(accept.getDemandId());
        if (demand == null || !demand.getPublisherId().equals(publisherId))
            throw new BusinessException("无权操作");

        DemandAccept u = new DemandAccept();
        u.setAcceptId(acceptId);
        u.setAcceptStatus(2);
        u.setRejectReason(rejectReason);
        acceptMapper.updateById(u);
    }

    // ========== 供需智能匹配 ==========

    /** 根据需求匹配供应 */
    public List<SupplyInfo> matchSupply(Long demandId) {
        DemandMain demand = getById(demandId);
        if (demand == null)
            throw new BusinessException("需求不存在");

        LambdaQueryWrapper<SupplyInfo> w = new LambdaQueryWrapper<>();
        w.eq(SupplyInfo::getDeleteFlag, 0)
                .eq(SupplyInfo::getAuditStatus, 1)
                .eq(SupplyInfo::getStatus, 1)
                .eq(StringUtils.hasText(demand.getCategory()), SupplyInfo::getCategory, demand.getCategory())
                .le(demand.getExpectPrice() != null, SupplyInfo::getPrice, demand.getExpectPrice())
                .ge(SupplyInfo::getStock, demand.getQuantity())
                .orderByAsc(SupplyInfo::getPrice)
                .last("LIMIT 20");
        return supplyInfoMapper.selectList(w);
    }

    /** 根据供应匹配需求 */
    public List<DemandMain> matchDemand(Long supplyId) {
        SupplyInfo supply = supplyInfoMapper.selectById(supplyId);
        if (supply == null)
            throw new BusinessException("供应信息不存在");

        LambdaQueryWrapper<DemandMain> w = new LambdaQueryWrapper<>();
        w.eq(DemandMain::getDeleteFlag, 0)
                .eq(DemandMain::getAuditStatus, 1)
                .in(DemandMain::getDemandStatus, 1, 2)
                .eq(StringUtils.hasText(supply.getCategory()), DemandMain::getCategory, supply.getCategory())
                .ge(DemandMain::getExpectPrice, supply.getPrice())
                .le(DemandMain::getQuantity, supply.getStock())
                .ge(DemandMain::getExpectTime, LocalDate.now())
                .orderByDesc(DemandMain::getExpectPrice)
                .last("LIMIT 20");
        return list(w);
    }

    // ========== 管理员操作 ==========

    /** 管理员审核 */
    public void audit(Long demandId, Integer auditStatus, String auditRemark) {
        DemandMain d = getById(demandId);
        if (d == null)
            throw new BusinessException("需求不存在");
        DemandMain u = new DemandMain();
        u.setDemandId(demandId);
        u.setAuditStatus(auditStatus);
        u.setAuditRemark(auditRemark);
        if (auditStatus == 1)
            u.setDemandStatus(1); // 审核通过自动发布
        updateById(u);
    }

    /** 管理员全量需求列表 */
    public Page<DemandMain> adminList(int page, int size, String title, Integer demandStatus,
            String publisherName, String startTime, String endTime) {
        LambdaQueryWrapper<DemandMain> w = new LambdaQueryWrapper<>();
        w.eq(DemandMain::getDeleteFlag, 0)
                .like(StringUtils.hasText(title), DemandMain::getTitle, title)
                .eq(demandStatus != null, DemandMain::getDemandStatus, demandStatus)
                .like(StringUtils.hasText(publisherName), DemandMain::getPublisherName, publisherName)
                .ge(StringUtils.hasText(startTime), DemandMain::getCreateTime, startTime)
                .le(StringUtils.hasText(endTime), DemandMain::getCreateTime, endTime)
                .orderByDesc(DemandMain::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    /** 管理员强制下架 */
    public void forceOffline(Long demandId) {
        DemandMain u = new DemandMain();
        u.setDemandId(demandId);
        u.setDemandStatus(0);
        updateById(u);
    }

    // ========== 内部方法 ==========

    private String generateDemandNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "DM" + date + String.format("%04d", new Random().nextInt(10000));
    }
}
