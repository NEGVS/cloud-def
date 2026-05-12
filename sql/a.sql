

-- auto-generated definition
create table job
(
    id                       int(11) unsigned auto_increment comment '主键ID'
        primary key,
    name                     varchar(50) collate utf8mb4_unicode_ci default ''                not null comment '岗位名称',
    job_nature               tinyint(2) unsigned                    default 1                 not null comment '岗位类型（工作性质）1:全职，2:兼职',
    job_type                 tinyint(3)                             default 1                 not null comment '岗位属性 1：商户岗位 2:摘星台岗位（目前已称为自营岗位）',
    one_category_id          int(11) unsigned                       default 0                 not null comment '岗位一级分类ID',
    category_id              int(11) unsigned                       default 0                 not null comment '岗位二级分类ID',
    recruitment_type         tinyint(2)                             default 1                 not null comment '招聘类型1:直招，2:派遣，3:代招',
    user_id                  int                                    default 0                 not null comment '商户端用户id',
    bonus_subsidy            varchar(200)                           default ''                not null comment '奖金补贴  1餐补、2房补、3保底工资、4加班费、5话补、6晚班补贴、7高温补贴 8交通补贴、9底薪加提成 10绩效奖金、11年终奖、12年底双薪、13全勤奖、14工龄奖、15法定节假日三薪',
    social_security_fund     tinyint                                default 0                 not null comment '社保公积金0 无 1：五险 2：五险一金 3:五险二金',
    welfares                 varchar(255)                           default ''                not null comment '企业福利  1员工旅游、2环境好、3老板好、4帅哥美女多、5不加班、6就近分配、7免费培训、8员工聚餐、9晋升空间大、10生日福利、11宿舍有空调、12有无线网、13免费工装',
    accommodation            tinyint(4) unsigned                    default 0                 not null comment '食宿情况: 1:包吃包住、2:包吃、3:包住、4:不包吃住',
    job_detail               text                                                             null comment '岗位详情',
    age_min                  tinyint(2) unsigned                    default 0                 not null comment '年龄要求-最小',
    age_max                  tinyint(2) unsigned                    default 0                 not null comment '年龄要求-最大',
    gender_require           tinyint(2) unsigned                    default 0                 not null comment '性别要求 0不限  1男 2女',
    education_require        tinyint(4) unsigned                    default 0                 not null comment '学历要求 0学历不限、1-初中及以下  2-初中 3-高中 4-大专 5-本科 6-研究生',
    other_require            varchar(300)                           default ''                not null comment '其他要求',
    company_id               int unsigned                           default 0                 not null comment '门店（公司）id',
    brand_id                 int                                    default 0                 not null comment '岗位主品牌ID（关联 brand.id；0 表示未指定）',
    group_id                 int                                    default 0                 not null comment '所属群ID',
    hire_all                 int(11) unsigned                       default 0                 not null comment '全部招聘人数',
    img_ids                  varchar(1000)                          default ''                not null comment '岗位照片id，对应image表',
    video_id                 int(10)                                default 0                 not null comment '视频ID，image表主键ID，type=video',
    video_image_id           int                                    default 0                 not null comment '视频ID，image表主键ID，type=video_image',
    source                   tinyint                                default 3                 not null comment '岗位创建来源。1:熟仁直聘管理后台 2:小程序 3：app 4:摘星台同步',
    last_auditor_id          int(10)                                default 0                 not null comment '最近一次审核人的ID（操作禁用/启用管理员ID）',
    down_reason              varchar(200)                                                     null comment '下架理由',
    disable_reason           varchar(200)                                                     null comment '禁用理由',
    updated_disable_admin_id int                                    default 0                 not null comment '最后一次操作禁用A端ID',
    updated_status_admin_id  int                                    default 0                 not null comment '最后一次操作状态A端ID',
    updated_status_user_id   int                                    default 0                 not null comment '最后一次更新状态的用户ID 有可能是自己有可能是管理员',
    status                   tinyint(3)                             default 4                 not null comment '状态 1-在招/招聘中 4-停招',
    is_disable               tinyint(2)                             default 0                 not null comment '是否禁用 0-否 1-是',
    is_open_phone            tinyint(2)                             default 1                 not null comment '是否开放手机 1-开放 2-未开发',
    release_time             timestamp                                                        null comment '最后一次发布时间',
    audit_at                 timestamp                                                        null comment '审核时间',
    audit_type               tinyint(4) unsigned                    default 100               not null comment '岗位审核状态 0=未审核，1=通过，2=不通过 100=默认 待提交审核（企业未审核通过或者没有岗位卡的情况为100）',
    audit_remark             varchar(100)                           default ''                not null comment '最后一次审核备注',
    audit_submit_at          timestamp                                                        null comment '审核提交时间',
    created_admin_id         int(11) unsigned                       default 0                 not null comment 'A端创建人ID',
    updated_admin_id         int                                    default 0                 not null comment '最近一次A端编辑人',
    updated_user_id          int                                    default 0                 not null comment '最近一次B端编辑人',
    last_job_audit_log_id    int                                    default 0                 not null comment '最后一次提交审核的审核日志ID',
    job_tag                  varchar(200)                           default ''                not null comment '自定义标签',
    is_hot                   tinyint                                default 0                 null comment '是否是热门岗位，1：是；0：否',
    created_at               timestamp                              default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at               timestamp                              default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted_at               timestamp                                                        null comment '删除时间',
    prop_status              smallint                               default 0                 null comment '道具状态 0=无 1=生效 2=暂停',
    prop_type                smallint                                                         null comment '道具类型 1=置顶急招 2=人才炸弹',
    prop_stage               smallint                                                         null comment '道具阶段 1/2/3',
    prop_order_id            bigint                                                           null comment '关联道具订单ID',
    prop_effect_time         datetime                                                         null comment '道具生效时间',
    prop_reached             tinyint                                default 0                 null comment '当前阶段是否达标 0=未达标 1=已达标'
)
    comment '岗位主表' collate = utf8mb4_bin;

create index idx_audit_submit_at
    on job (audit_submit_at);

create index idx_brand_id
    on job (brand_id);

create index idx_category_id
    on job (category_id);

create index idx_com
    on job (company_id);

create index idx_created_at
    on job (created_at);

create index idx_group_id
    on job (group_id);

create index idx_hire_all
    on job (hire_all);

create index idx_name
    on job (name);

create index idx_prop_status
    on job (prop_status);

create index idx_user_id
    on job (user_id);

create index updated_at
    on job (updated_at);


