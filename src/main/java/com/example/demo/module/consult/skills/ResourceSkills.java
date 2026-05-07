package com.example.demo.module.consult.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资源技能层。
 * <p>
 * 提供校园心理咨询的资源链接：
 * <ul>
 *   <li>校内资源</li>
 *   <li>校外资源</li>
 *   <li>危机资源</li>
 * </ul>
 */
@Slf4j
@Service
public class ResourceSkills {

    /** 心理援助热线 */
    private static final String PSYCHOLOGICAL_HOTLINE = "400-161-9995";

    /**
     * 获取校内资源
     *
     * @return 校内资源列表
     */
    public List<CampusResource> getCampusResources() {
        return List.of(
                new CampusResource("心理咨询中心", "预约咨询", "行政楼301"),
                new CampusResource("辅导员", "日常支持", "各学院"),
                new CampusResource("心理委员", "朋辈支持", "各班级")
        );
    }

    /**
     * 获取校外资源
     *
     * @return 校外资源列表
     */
    public List<ExternalResource> getExternalResources() {
        return List.of(
                new ExternalResource("全国心理援助热线", PSYCHOLOGICAL_HOTLINE, "24小时"),
                new ExternalResource("医院心理科", "专业治疗", "各医院")
        );
    }

    /**
     * 获取危机资源
     *
     * @return 危机资源列表
     */
    public List<CrisisResource> getCrisisResources() {
        return List.of(
                new CrisisResource("紧急热线", "110", "生命危险"),
                new CrisisResource("心理危机热线", PSYCHOLOGICAL_HOTLINE, "心理危机")
        );
    }

    /**
     * 生成资源推荐文本
     *
     * @return 资源推荐文本
     */
    public String generateResourceRecommendation() {
        return "以下资源可能对你有帮助：\n\n"
                + "校内资源：\n"
                + "- 心理咨询中心：行政楼301\n"
                + "- 辅导员：各学院\n"
                + "- 心理委员：各班级\n\n"
                + "校外资源：\n"
                + "- 全国心理援助热线：" + PSYCHOLOGICAL_HOTLINE + "（24小时）\n"
                + "- 医院心理科：各医院";
    }

    /**
     * 校内资源内部类
     */
    public static class CampusResource {
        private String name;
        private String service;
        private String location;

        public CampusResource(String name, String service, String location) {
            this.name = name;
            this.service = service;
            this.location = location;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    /**
     * 校外资源内部类
     */
    public static class ExternalResource {
        private String name;
        private String contact;
        private String availability;

        public ExternalResource(String name, String contact, String availability) {
            this.name = name;
            this.contact = contact;
            this.availability = availability;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        public String getAvailability() { return availability; }
        public void setAvailability(String availability) { this.availability = availability; }
    }

    /**
     * 危机资源内部类
     */
    public static class CrisisResource {
        private String name;
        private String contact;
        private String purpose;

        public CrisisResource(String name, String contact, String purpose) {
            this.name = name;
            this.contact = contact;
            this.purpose = purpose;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }
}
