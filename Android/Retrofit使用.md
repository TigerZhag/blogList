## get方法

方法注解:
1. @streaming 下载大文件时流式下载
2. @get("path/{var}") 下载路径

参数注解:
1. @Path(var) 替换方法注解中的变量
2. @Query("") 参数
3. @QueryMap("") 参数map


## Post方法
方法注解:

> 1,2和浏览器请求类似, 浏览器原生form表单也只支持这两种方式, ajax等框架会支持诸如 application/json ,text/xml 等请求格式
1. @FormUrlEncode 表明请求格式application/x-www-form-urlencoded, 提交数据方式之一, 最常见, 浏览器原生form表单提交方式, 如不添加enctype属性,默认为此方式, 请求体提交格式为: param1=xxxx&param2=xxxxx....
2. @multipart 表明请求格式multipart/form-data ,提交数据方式之一,我们使用表单上传文件时，必须让 <form> 表单的 enctype 等于 multipart/form-data。
3. @Post("path/{var}") 提交路径

参数注解:
1. @Field("") RequestBody param1 : application/x-www-form-urlencoded请求格式专用, 设置参数键值对
2. @Part("") RequestBody param2 : multipart/form-data请求格式专用, 设置键值对
3. @PartMap Map<String, Request> param: multipart/form-data请求格式专用, 设置键值对

