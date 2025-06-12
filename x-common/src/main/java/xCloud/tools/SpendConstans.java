package xCloud.tools;


public interface SpendConstans
{
   /**
    * 请求类型
    */
   public final static String TARGET_CONTENT_TYPE = "application/json";

   /**
    * UTF-8编码
    */
   public static final String UTF8_ENCODE = "UTF-8";

   /**
    * 应用ID
    */
   //         public final static String APP_ID = "648ab2f6cab8d96e690942ec";
   //   public final static String APP_ID = "6486cfc72536f61346669ec7";

   /**
    * 应用秘钥
    */
   //         public final static String APP_KEY = "648ab35d2167bf6e6e080c98";
   //   public final static String APP_KEY = "64895cd215551a0bcfd9ed38";

   /**
    * 环境地址
    */
   //            public final static String HOST_URL = "https://openapi-fat2.fenbeijinfu.com";
   //   public final static String HOST_URL = "https://openapi.fenbeitong.com";

   /**
    * 模板ID
    */

   /**
    * userID
    */

   /**
    * 请求头token
    */
   public final static String ACCESS_TOKEN = "access-token";

   /**
    * token生成地址 
    */
   public final static String TOKEN_API = "/openapi/auth/getToken";

   /**
    * 批量绑定项目三方ID
    */
   public final static String INT_ADD_PROJECT_BIND = "/openapi/org/project/v1/bind";

   /**
    * 新增项目-公司URL 
    * 调用此接口在公司中新增项目信息（暂不支持批量）
    */
   public final static String INT_ADD_PROJECT_CREATE = "/openapi/org/project/v1/create";

   /**
    * 更新项目-公司URL 
    * 调用此接口在公司中更新项目信息（暂不支持批量）
    */
   public final static String INT_ADD_PROJECT_UPDATE = "/openapi/org/project/v1/update";

   /**
    * 删除项目-公司URL 
    * 调用此接口删除公司项目（暂不支持批量）      
    * 启用状态下的项目不可在分贝通删除
    */
   public final static String INT_ADD_PROJECT_DELETE = "/openapi/org/project/v1/delete";

   /**
    * 批量新增/更新项目自定义字段-公司URL 
    */
   public final static String INT_ADD_PROJECT_CUSTOM_FIELD_UPDATE = "/openapi/org/project/v1/custom_field_update";

   /**
    * 获取项目自定义字段列表-公司URL 
    */
   public final static String INT_ADD_PROJECT_CUSTOM_FIELD_LIST = "/openapi/org/project/v1/custom_field_list";

   /**
    * 批量更新项目状态
    * 调用此接口批量更新企业项目状态
    */
   public final static String INT_ADD_PROJECT_STATE_UPDATE = "/openapi/org/project/v1/state_update";

   /**
    * 获取项目信息列表
    * 调用此接口可获取分贝通已有的所有项目信息
    */
   public final static String INT_ADD_PROJECT_LIST = "/openapi/org/project/v1/list";

   /**
    * 获取项目信息详情
    * 调用此接口可获取分贝通已有的项目信息详情
    */
   public final static String INT_ADD_PROJECT_DETAIL = "/openapi/org/project/v1/detail";

   /**
    * 项目分组绑定三方ID
    * 调用此接口可根据项目分组名称绑定三方系统的项目分组ID
    */
   public final static String INT_ADD_PROJECT_GROUP_BIND = "/openapi/org/project/v1/group_bind";

   /**
    * 新增项目分组-公司URL 
    * 调用此接口新增公司项目分组信息（暂不支持批量）
    */
   public final static String INT_ADD_PROJECT_GROUP_CREATE = "/openapi/org/project/v1/group_create";

   /**
    * 更新项目分组-公司URL 
    * 调用此接口更新公司项目分组信息（暂不支持批量）
    */
   public final static String INT_ADD_PROJECT_GROUP_UPDATE = "/openapi/org/project/v1/group_update";

   /**
    * 删除项目分组-公司URL 
    * 调用此接口删除公司项目分组信息（暂不支持批量
    */
   public final static String INT_ADD_PROJECT_GROUP_DELETE = "/openapi/org/project/v1/group_delete";

   // 获取表单模版列表
   public final static String INT_ADD_COMMON_CUSTOM_FORM_LIST = "/openapi/common/custom_form/v1/form_list";

   // 获取表单模版详情
   public final static String INT_ADD_COMMON_CUSTOM_FORM_DETAIL = "/openapi/common/custom_form/v1/form_detail";

   // 批量新增供应商（自定义表单）
   public final static String INT_ADD_PYMENT_CUSTOM_SUPPLIER_CREATE = "/openapi/payment/custom_supplier/v1/create";

   // 批量更新供应商（自定义表单）
   public final static String INT_ADD_PYMENT_CUSTOM_SUPPLIER_UPDATE = "/openapi/payment/custom_supplier/v1/update";

   // 批量更新供应商状态（自定义表单）
   public final static String INT_ADD_PYMENT_CUSTOM_SUPPLIER_UPDATE_STATE = "/openapi/payment/custom_supplier/v1/update_state";

   // 批量删除供应商（自定义表单）
   public final static String INT_ADD_PYMENT_CUSTOM_SUPPLIER_DELETE = "/openapi/payment/custom_supplier/v1/delete";

   // 获取供应商列表
   public final static String INT_ADD_PYMENT_CUSTOM_SUPPLIER_LIST = "/openapi/payment/custom_supplier/v1/list";

   // 获取供应商详情（自定义表单）
   public final static String INT_ADD_PYMENT_CUSTOM_SUPPLIER_DETAIL = "/openapi/payment/custom_supplier/v1/detail";

}
