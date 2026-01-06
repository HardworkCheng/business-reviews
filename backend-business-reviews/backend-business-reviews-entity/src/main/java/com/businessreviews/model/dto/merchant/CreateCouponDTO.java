package com.businessreviews.model.dto.merchant;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户端创建优惠券请求传输对象
 * <p>
 * 封装创建优惠券所需的各类参数，与数据库表 coupons 字段对应
 * </p>
 * 
 * 优惠券类型说明：
 * - type=1 满减券：需要 amount（优惠金额）和 minAmount（最低消费）
 * - type=2 折扣券：需要 discount（折扣，如0.8表示8折）和 minAmount（最低消费）
 * - type=3 代金券：需要 amount（代金金额），无使用门槛
 * 
 * @author businessreviews
 */
@Data
public class CreateCouponDTO {

    /** 适用门店ID（空表示全部门店） */
    private Long shopId;

    /** 券类型（1满减券，2折扣券，3代金券） */
    @NotNull(message = "券类型不能为空")
    private Integer type;

    /** 券标题 */
    @NotBlank(message = "券标题不能为空")
    @Size(max = 100, message = "券标题不能超过100个字符")
    private String title;

    /** 使用说明 */
    @Size(max = 500, message = "使用说明不能超过500个字符")
    private String description;

    /** 优惠金额（满减券、代金券使用） */
    @DecimalMin(value = "0.01", message = "优惠金额必须大于0")
    private BigDecimal amount;

    /** 折扣（折扣券使用，如0.8表示8折） */
    @DecimalMin(value = "0.01", message = "折扣必须大于0.01")
    @DecimalMax(value = "0.99", message = "折扣必须小于0.99")
    private BigDecimal discount;

    /** 最低消费金额 */
    @DecimalMin(value = "0", message = "最低消费金额不能为负数")
    private BigDecimal minAmount;

    /** 发放总量 */
    @NotNull(message = "发放总量不能为空")
    @Min(value = 1, message = "发放总量必须大于0")
    private Integer totalCount;

    /** 每人限领次数 */
    @Min(value = 1, message = "每人限领次数必须大于0")
    private Integer perUserLimit;

    /** 生效时间 */
    @NotNull(message = "生效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 失效时间 */
    @NotNull(message = "失效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 是否可叠加（true可叠加，false不可叠加） */
    private Boolean stackable;
}
