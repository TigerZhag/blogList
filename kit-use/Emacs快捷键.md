# 总
C-u (n) 操作 重复几遍该命令
C-x 扩展快捷键
M-x 扩展命令

# 光标移动
C-p 上一行
C-n 下一行
C-f 前一格
C-b 后一格

M-f 前一个词
M-b 后一个词

C-v 下一页
M-v 上一页
C-l 置于中央

C-a 行首
C-e 行尾

# 文本操作
C-space/@ 标记
C-w 移除
C-d delete
C-backspace 回删一个词
m-d delete一个词
C-k 移除一行

C-y 召回
M-y 召回前一个（循环）

C-x C-w 写入到文件

C-x C-l/u 选中区域大小写转换
M-u/l 将光标所在出到词尾文本转换未大写/小写

C-t 调换光标左右单个字符
M-t 调换光标左右单个单词
C-x C-t 调换光标所在行和前一行
C-o 光标下方打开新行，不移动光标


# 查找与替换
C-s 搜索之后的内容
C-s ==> C-s 下一个结果
C-s ==> backspace 上一个结果
C-r 搜索之前的内容

C-M-s 正则表达式前向搜索
C-M-r 正则表达式后向搜索

M-x replace-string xxx RET yyy 将前向所有xxx替换为yyy 
M-x replace-regexp 正则表达式替换

M-% / M-x query-replace 前向搜索替换，询问每个实例处理方式
C-M-% / M-x query-replace-regexp 前向搜索正则表达式，询问每个实例处理方式

# window
C-x 3 横向分屏
C-x 2 竖向分屏
C-x 1 只保留当前window
C-x 0 关闭当前Window
C-x o 切换光标所在Window
C-M-v 翻滚另一个Window

# buffer
C-x b buffer-name 切换buffer
C-mouse-1 弹框显示所有buffer，RET进入
C-x C-b 查看buffer列表
M-x rename-buffer 重命名当前buffer
C-x k 关闭buffer
M-x kill-some-buffers 删除一些buffer
C-x C-s 保存当前buffer
C-x s 保存所有buffer
C-x C-q 切换buffer只读或读写状态

# 文件管理
C-x d 进入Dired列表模式{
移动：
	n: 下一个文件
	p： 上一个文件
	>: 下一个文件夹  
	<: 上一个文件夹
	M-}： 向下查看标记
	M-{: 向上查看标记
查看：
	g: 刷新
	i: 插入一个子目录
	v: 在当前窗口查看
	o: 在另一个窗口查看
	y: 查看文件类型
	=: 比较文件

标记：
	m: 标记
	u: 取消标记
	U: 取消所有标记
	*/ : 标记所有文件夹
	**: 标记所有可执行文件
	d: 标记为删除
    x: 执行删除
	～: 将所有备份文件标记为删除
    #: 将存盘文件标记为删除
	%m: 根据正则改变标记文件
	%g: 根据正则改变标记文件内容
	
其他：
	C-x C-f 
	+: 新建目录
	C-x C-q 文件列表设置为只读
}

# mode

Fundamental : 默认，最少设置和绑定
Text        : 编辑文本的基本模式
Abbrev      : 用于生成和使用缩进
Auto-fill   : 用于自动文字回绕,填充较长的行和段落
Over-write  : 用于覆盖缓冲区中任何现有的文本 ,而不是在当前位置插入文本
C           : 编辑C程序源码
Line number : 显示行号
Tex         : 编辑Tex文档
WordStar    : 提供了WordStar的键绑定
Lisp interaction: 编辑编译Lisp代码

## cua-mode
C-x r t 在选定行前插入
C-x r k 剪切一个矩形块
C-x r o 插入一个矩形块
C-x r y 粘贴一个矩形块
C-x r c 清除一个矩形块

按C-Enter进入矩形操作模式
M-a: 将rect中的文字左对齐
M-b: 用空格(tabs或者spaces)替换所有rect中的字符
M-c: 去掉所有行左侧的空格
M-f: 用单个字符替换所有rect中的字符(提示输入一个字符)
[M-i]: 对每行中第一个找到的数字进行加1操作(自动把0x开头的当作十六进制数字)
M-k: 剪切rect
M-l: 把rect中的内容全部转换为小写
M-m: 拷贝rect
[M-n]: 用一串自增的数字替换rect中的每一行(这个功能可以用来给每行编号)
[M-o]: rect的内容右移，选中的rect用空格填充
[M-r]: 用字符串替换符满足正则表达式的字符串
M-R: 上下反转
[M-s]: 把rect中的每一行替换为一个字符串(提示输入)
[M-t]: 把rect整个替换为一个字符串(提示输入)
[M-u]: 把rect中的内容全部转换为大写
M-|: 对rect执行一个shell命令
