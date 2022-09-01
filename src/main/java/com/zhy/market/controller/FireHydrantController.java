package com.zhy.market.controller;

import com.zhy.market.domain.RepairInfo;
import com.zhy.market.mapper.FireHydrantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("FireHydrant")
public class FireHydrantController {

    @Autowired
    private FireHydrantMapper fireHydrantMapper;

    @GetMapping("getAllFireHydrantInfo")
    public Object getAllFireHydrantInfo() {
        return fireHydrantMapper.getAllFireHydrantInfo();
    }

    @GetMapping("getFireHydrantInfoById")
    public Object getFireHydrantInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getFireHydrantInfoById FireHydrant" + id);
        return getJsonRes(0, "获得消防栓信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }

    @GetMapping("updateFireHydrantRepairInfoWithId")
    public Object updateFireHydrantRepairInfoWithId(@RequestBody RepairInfo repairInfo) {
        String repairInfoText = repairInfo.repairInfo;
        Integer id = repairInfo.id;

        Integer res = fireHydrantMapper.updateFireHydrantRepairInfoWithId(repairInfoText, id);
        if (res == 1) {
            return getJsonRes(0, "更新维修记录成功");
        } else {
            return getJsonRes(1, "更新维修记录失败");
        }
    }

    @GetMapping("getFireHydrantRepairInfoById")
    public Object getFireHydrantRepairInfoById(HttpServletRequest request) {
        Integer id = Integer.valueOf(request.getParameter("id"));
        System.out.println("getFireHydrantRepairInfoById FireHydrant" + id);
        return getJsonRes(0, "获得消防栓维修信息成功", fireHydrantMapper.getFireHydrantRepairInfoById(id));
    }
}
