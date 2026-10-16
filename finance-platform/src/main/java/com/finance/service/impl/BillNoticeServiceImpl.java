package com.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.entity.BillNotice;
import com.finance.mapper.BillNoticeMapper;
import com.finance.service.BillNoticeService;
import org.springframework.stereotype.Service;

@Service
public class BillNoticeServiceImpl extends ServiceImpl<BillNoticeMapper, BillNotice> implements BillNoticeService {
}