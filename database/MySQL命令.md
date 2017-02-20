# 连接MySQL
连接本地：mysql -u xxx -p RET xxx
连接远程：mysql -h110.110.110.110 -u root -p xxx;

退出： exit

# 修改密码
mysqladmin -u xxx -p yyy password zzz

> 以下命令在sql环境中
# 用户管理
grant select,insert,delete on dbname.* to 用户名@登录主机 identified by “password”

eg 1.
grant select,insert,update,delete on *.* to [email=test1@”%]test1@”%[/email]” Identified by “abc”;
eg 2.
grant select,insert,update,delete on mydb.* to [email=test2@localhost]test2@localhost[/email] identified by "a";

# 数据库操作
创建：create database <name>;
显示：show databases;

修改编码: 
1. my.ini 配置文件中修改default-character-set=gbk
2. 代码中 jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=gbk

删除数据库
drop database <name> if exists;

连接数据库
use <name>

> select命令相当于print或者write语句，可以显示一些结果
当前连接的数据库： select database();
版本:             select version();
时间：            select now();
日期：            select DAYOFMONTH(CURRENT_DATE);
字符串：          select “abcdefghijklmnopq";
计算器：          select (4*4)+25;
串接字符串：      select CONCAT(f_name,"",l_name) AS Name from xxxx where title='xxx';

# 表
1. 创建： create table <name>(<字段1><类型>。。。);
2. 删除： drop table MyTables if exists;
3. 插入： insert into <name> values(xxx,xxx,'xxx','xxx');
4. 查询： select xxx,xxx,xxx from <tableName> [where] [order by] [limit M,N] ;
5. 修改内容： update <tableName> set xxx='xxx' where xxx;
6. 修改表结构：
7. 增加字段： alter table <tablename> add xxx int(4) default '0';
8. 修改字段名: alter table <tablename> change origin_name new_name field_type;
9. 删除字段： alter table <tablename> drop field_name;
10. 加索引   : alter table <tablename> add [index/primary/unique] xxx(aa,bb);
11. 删除索引： alter table <tablename> drop index xxx;
12. 修改表名： rename table xxx to yyy;

# 备份
1. 导出数据库 mysqldump -u xxx -p database_name > outfile_name.sql
2. 导出一个表 mysqldump -u xxx -p database_name table_name > outfile_name.sql
3. 导出一个数据库结构 mysqldump -u xxx -p -d -add-drop-table database_name > outfile_name.sql
4. 带语言参数的输出：mysqldump -uroot -p –default-character-set=latin1 –set-charset=gbk –skip-opt database_name > outfile_name.sql

# 多表连接查询
## 外连接
left <outer>  join
right <outer> 
full <outer> join
## 内连接

## 交叉连接

## 一对多/多对一/多对多 连接语句

# MySQL中的函数
## 内置函数
### 数值函数
Abs(X)，绝对值abs(-10.9) = 10

Format(X，D)，格式化千分位数值format(1234567.456, 2) =1,234,567.46

Ceil(X),向上取整ceil(10.1) = 11

Floor(X),向下取整floor (10.1) = 10

Round(X),四舍五入去整

Mod(M,N) M%N M MOD N 求余 10%3=1

Pi(),获得圆周率

Pow(M,N) M^N

Sqrt(X)，算术平方根

Rand(),随机数

TRUNCATE(X,D) 截取D位小数

### 时间日期函数

Now(),current_timestamp(); 当前日期时间

Current_date();当前日期

current_time();当前时间

Date(‘yyyy-mm-dd HH;ii:ss’);获取日期部分

Time(‘yyyy-mm-dd HH;ii:ss’);获取时间部分

Date_format(‘yyyy-mm-dd HH;ii:ss’,’%D %y %a %d %m %b %j');

Unix_timestamp();获得unix时间戳

From_unixtime();//从时间戳获得时间

### 字符串函数

LENGTH(string ) //string长度，字节

CHAR_LENGTH(string) //string的字符个数

SUBSTRING(str ,position [,length ]) //从str的position开始,取length个字符

REPLACE(str ,search_str ,replace_str) //在str中用replace_str替换search_str

INSTR(string ,substring ) //返回substring首次在string中出现的位置

CONCAT(string [,... ]) //连接字串

CHARSET(str) //返回字串字符集

LCASE(string ) //转换成小写

LEFT(string ,length ) //从string2中的左边起取length个字符

LOAD_FILE(file_name) //从文件读取内容

LOCATE(substring , string [,start_position ]) //同INSTR,但可指定开始位置

LPAD(string ,length ,pad ) //重复用pad加在string开头,直到字串长度为length

LTRIM(string ) //去除前端空格

REPEAT(string ,count ) //重复count次

RPAD(string ,length ,pad) //在str后用pad补充,直到长度为length

RTRIM(string ) //去除后端空格

STRCMP(string1 ,string2 ) //逐字符比较两字串大小
### 流程函数：

CASE WHEN [condition]THEN result[WHEN [condition]THEN result ...][ELSE result]END 多分支
IF(expr1,expr2,expr3) 双分支。

### 聚合函数

Count()
Sum();
Max();
Min();
Avg();
Group_concat()

### 其他常用函数

Md5();
Default();

## 自定义函数

### 新建
create function function_name(参数列表) returns 返回值类型
函数体
### 删除
drop function if exists function_name;
