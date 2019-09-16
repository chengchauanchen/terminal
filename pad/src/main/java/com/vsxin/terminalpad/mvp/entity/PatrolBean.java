package com.vsxin.terminalpad.mvp.entity;

import com.ixiaoma.xiaomabus.architecture.bean.BaseBean;

import java.util.Map;

/**
 * 巡逻船
 *
 */
public class PatrolBean extends BaseBean {
    private String patrolNo;//巡逻船编号
    private String patrolName;//巡逻船名称
    private String patrolOccupant;//所在部门
    private String patrolLat;
    private String patrolLng;
    private Map<String,PersonnelBean> personnelDtoMap;//key:88021206,value:警员map

    public String getPatrolNo() {
        return patrolNo;
    }

    public void setPatrolNo(String patrolNo) {
        this.patrolNo = patrolNo;
    }

    public String getPatrolName() {
        return patrolName;
    }

    public void setPatrolName(String patrolName) {
        this.patrolName = patrolName;
    }

    public String getPatrolOccupant() {
        return patrolOccupant;
    }

    public void setPatrolOccupant(String patrolOccupant) {
        this.patrolOccupant = patrolOccupant;
    }

    public String getPatrolLat() {
        return patrolLat;
    }

    public void setPatrolLat(String patrolLat) {
        this.patrolLat = patrolLat;
    }

    public String getPatrolLng() {
        return patrolLng;
    }

    public void setPatrolLng(String patrolLng) {
        this.patrolLng = patrolLng;
    }

    public Map<String, PersonnelBean> getPersonnelDtoMap() {
        return personnelDtoMap;
    }

    public void setPersonnelDtoMap(Map<String, PersonnelBean> personnelDtoMap) {
        this.personnelDtoMap = personnelDtoMap;
    }
}

/**
 *  * "patrol": [{
 *  * 		"patrolNo": "巡逻船B",
 *  * 		"patrolName": "听涛派出所,巡逻船B",
 *  * 		"patrolOccupant": "听涛派出所",
 *  * 		"patrolLat": "30.475625040574773",
 *  * 		"patrolLng": "114.41593570510226",
 *  * 		"personnelDtoMap": {
 *  * 			"88021206": {
 *  * 				"personnelName": "测试小大",
 *  * 				"personnelNo": "88021206",
 *  * 				"personnelLat": "30.475625040574773",
 *  * 				"personnelLng": "114.41593570510226",
 *  * 				"terminalDtoList": [{
 *  * 					"terminalType": "TERMINAL_PHONE",
 *  * 					"terminalGroupNo": "100040",
 *  * 					"terminalUniqueNo": "156015518354393289"
 *  *                                }]* 			},
 *  * 			"88021204": {
 *  * 				"personnelName": "测试小大",
 *  * 				"personnelNo": "88021204",
 *  * 				"personnelLat": "30.475625040574773",
 *  * 				"personnelLng": "114.41593570510226",
 *  * 				"terminalDtoList": [{
 *  * 					"terminalType": "TERMINAL_PHONE",
 *  * 					"terminalGroupNo": "100040",
 *  * 					"terminalUniqueNo": "156015518354393286"
 *  * 				}]
 *  *                    }* 		}
 *  * 	}]
 */

