# distribution
1. Red Hat
2. Debian
3. Fedora
4. Ubuntu
5. CentOS
6. ...

# 套件管理
Debian : pkg
Red Hat: RPM

# 查看帮助
man
whatis
apropos

# 单人维护模式
grub
lilo

# 文件管理
## 相关命令
查看 ls [-a -l -lda]
drwxr-xr-x  2   root  root    4096 2月 19  2015 xml
    属性   连结 拥有者 群组    容量   修改日期  文件名

> tips: 用户需要目录的x权限才可进入该目录

修改权限
chgrp : 修改所属群组
chown : 修改所属人
chmod : 修改权限等属性

-R 递归操作,常用在目录
使用数字: chmod [-R] 777 filename
使用字符: chmod [a/u/g/o+/-r/w/x][a/u/g/o=rwx] filename

## 文件类型
1. 正规文件                 -
2. 目录                     d
3. 连结档 : 类似快捷方式     l
4. 设备与装置文件 , 通常集中于dev目录下
   block                    b
   character                c
....

## 文件管理
目录操作 :
cd 变幻目录
> 特殊目录:
1. .此层目录
2. ..上层目录
3. -前一个工作目录
4. ~用户目录
5. ~user user的用户目录

pwd    : 显示目前目录
mkdir  : 建立一个新目录 -m 777预设权限 -p 自动递归创建
rmdir  : 删除目录       -m 连同上层空目录一起删除

环境变量 $PATH

## 文件压缩打包
1. compress
2. gzip, zcat
3. bzip, bzcat
4. tar
5. dd
6. cpio

## shell
Bash shell

1. 设定命令别名 alias xx='xx -xx -xx'; 删除别名 unalias xx
3. shell scripts
4. 万用字符
5. type命令 查看指令类型(内部外部
2. 前景背景控制
&      : 放在一个命令后,可把命令放在后台执行
Ctrl+z : 将正在执行的命令切换到后台并暂停
jobs   : 查看当前有多少在后台执行的命令
fg     : 调至前台,默认第一个命令
bg     : 将后台中的进程继续执行
nohub  : 退出后继续执行
kill   : 给进程发送信号; 终止进程 用法: kill %num(job号)/kill pid
C-c    : 终止前台进程

变量:
设置变量 xxx=xxx/"xxx xxx"/'xxx'
查看变量 echo $xxx
删除变量 unset xxx
列出环境变量 env, export
列出自定变量 set

几个特殊变量:
PS1 : 命令提示字符
$   : PID
?   : 上个指令的回传码

## 正则
* 0或多个字符
? 一个字母
/# 批注,常用于script
\ 转义字符
| 分割两个管线命令的界定
; 连续性命令的界定
~ 用户目录
$ 取值
& 将指令变成背景下工作
! 非
/ 路径分隔的符号
/>,>> 输出导向,分别是取代和累加
' 不具有变量置换的功能
" 具有变量置换的功能
` ` 两个`中间为可以先执行的指令
() 在中间为子shell的起始与结束
[] 中间为字符的组合
{} 中间为命令区块的组合
$() 取值

## 数据流重导向
将指令的输出指向文件/装置等..

## 管线命令
1. 撷取命令 cut ,grep
cut :
-d "分隔字符" : 相当于java里的split("分隔字符");
-f field      : 取-d分割出的第field段
-c 字符区间   : 取得每一行区间内的字符

grep :
grep -[acinv] "搜寻字符串" filename
-a : 将binary档案按文本档案读取
-c : 计算符合条件次数
-i : 忽略大小写
-n : 输出行号
-v : 非

2. 排序命令 sort, wc,uniq
sort : 排序
wc   : 统计
uniq : 去重

3. 双向重导向 tee
tee [-a] filename

4. 字符转换命令
tr : 字符替换/删除/
col  : 将TAB替换成空格
join : 连结
paste : 连结
head : -n 输出前几行
split : 分割文件

4. printf

5. sed 分析STDIN 输出到STDOUT
sen [-nefr] [动作]


## 正则表达式



## 自定变量转成环境变量
export
