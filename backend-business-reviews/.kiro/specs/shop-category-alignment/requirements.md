# Requirements Document

## Introduction

本需求旨在统一商家运营中心的经营类目选项与UniApp移动端首页展示的八个类目，确保前后端数据一致性和用户体验的统一性。

## Glossary

- **商家运营中心**: 商家管理后台Web应用，用于商家管理店铺信息、优惠券等
- **UniApp**: 移动端应用，用户浏览店铺和笔记的主要入口
- **经营类目**: 店铺所属的业务分类，用于筛选和展示
- **类目ID**: 数据库中categories表的主键，用于关联店铺和类目
- **店铺表单**: 商家运营中心中编辑店铺信息的表单界面

## Requirements

### Requirement 1

**User Story:** 作为商家，我想在商家运营中心修改店铺信息时，看到与UniApp首页一致的八个经营类目选项，以便准确选择我的店铺类型。

#### Acceptance Criteria

1. WHEN 商家访问店铺信息管理页面 THEN 系统 SHALL 在经营类目下拉框中显示与UniApp首页完全一致的八个类目选项
2. WHEN 商家选择经营类目 THEN 系统 SHALL 正确保存对应的类目ID到数据库
3. WHEN 商家查看已保存的店铺信息 THEN 系统 SHALL 正确显示之前选择的经营类目名称
4. WHEN UniApp用户按类目筛选店铺 THEN 系统 SHALL 返回该类目下的所有店铺
5. WHEN 商家修改经营类目后 THEN UniApp端 SHALL 立即反映最新的类目信息

### Requirement 2

**User Story:** 作为开发人员，我想确保前后端使用相同的类目数据源，以便维护数据一致性和避免硬编码问题。

#### Acceptance Criteria

1. WHEN 系统初始化时 THEN 系统 SHALL 从数据库categories表读取类目列表
2. WHEN 类目数据发生变化时 THEN 前端 SHALL 能够动态获取最新的类目列表
3. WHEN 前端请求类目列表 THEN 后端 SHALL 返回包含id、name、icon、color等完整信息的类目数据
4. WHEN 保存店铺信息时 THEN 系统 SHALL 验证提交的类目ID在categories表中存在
5. WHEN 类目名称或顺序调整时 THEN 系统 SHALL 无需修改前端硬编码即可生效

### Requirement 3

**User Story:** 作为系统管理员，我想确保八个类目的名称、顺序和图标与UniApp首页完全一致，以便提供统一的用户体验。

#### Acceptance Criteria

1. WHEN 查看类目列表 THEN 系统 SHALL 按照sort_order字段升序排列类目
2. WHEN 显示类目选项 THEN 系统 SHALL 使用与UniApp首页相同的类目名称（美食、KTV、美发、美甲、足疗、美容、游乐、酒吧）
3. WHEN 数据库中的类目名称与UniApp不一致 THEN 系统 SHALL 提供数据修正方案
4. WHEN 类目数量超过或少于八个 THEN 系统 SHALL 仅显示status=1且sort_order在1-8范围内的类目
5. WHEN 类目图标需要展示时 THEN 系统 SHALL 使用categories表中存储的icon字段值
